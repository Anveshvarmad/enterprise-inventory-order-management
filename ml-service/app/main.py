import math
import os
from datetime import datetime
from typing import Any

import numpy as np
import pandas as pd
import psycopg2
from fastapi import FastAPI
from pydantic import BaseModel
from sklearn.ensemble import RandomForestRegressor


app = FastAPI(
    title="Inventory Demand Forecasting Service",
    version="1.0.0"
)


class ForecastItem(BaseModel):
    productId: int
    sku: str
    productName: str
    warehouseId: int
    warehouseCode: str
    warehouseName: str
    quantityOnHand: int
    reservedQuantity: int
    availableQuantity: int
    reorderLevel: int
    predictedDemand7Days: int
    predictedDailyDemand: float
    estimatedDaysUntilStockout: float
    recommendedReorderQuantity: int
    riskLevel: str


class ForecastResponse(BaseModel):
    generatedAt: str
    modelName: str
    modelVersion: str
    totalItemsScored: int
    highRiskItems: int
    mediumRiskItems: int
    lowRiskItems: int
    forecasts: list[ForecastItem]


def db_connection():
    return psycopg2.connect(
        host=os.getenv("POSTGRES_HOST", "localhost"),
        port=os.getenv("POSTGRES_PORT", "5432"),
        dbname=os.getenv("POSTGRES_DB", "inventory_db"),
        user=os.getenv("POSTGRES_USER", "inventory_user"),
        password=os.getenv("POSTGRES_PASSWORD", "inventory_password"),
    )


def load_training_data() -> pd.DataFrame:
    sql = """
        WITH sales AS (
            SELECT
                oi.product_id,
                oi.warehouse_id,
                COALESCE(SUM(CASE WHEN o.created_at >= CURRENT_TIMESTAMP - INTERVAL '30 days'
                    AND o.order_status <> 'CANCELLED' THEN oi.quantity ELSE 0 END), 0) AS qty_30d,
                COALESCE(SUM(CASE WHEN o.created_at >= CURRENT_TIMESTAMP - INTERVAL '60 days'
                    AND o.order_status <> 'CANCELLED' THEN oi.quantity ELSE 0 END), 0) AS qty_60d,
                COALESCE(SUM(CASE WHEN o.created_at >= CURRENT_TIMESTAMP - INTERVAL '90 days'
                    AND o.order_status <> 'CANCELLED' THEN oi.quantity ELSE 0 END), 0) AS qty_90d,
                COUNT(DISTINCT CASE WHEN o.created_at >= CURRENT_TIMESTAMP - INTERVAL '90 days'
                    AND o.order_status <> 'CANCELLED' THEN o.id ELSE NULL END) AS orders_90d
            FROM order_items oi
            JOIN customer_orders o ON o.id = oi.order_id
            GROUP BY oi.product_id, oi.warehouse_id
        )
        SELECT
            p.id AS product_id,
            p.sku,
            p.name AS product_name,
            p.unit_price,
            p.reorder_level,
            w.id AS warehouse_id,
            w.code AS warehouse_code,
            w.name AS warehouse_name,
            i.quantity_on_hand,
            i.reserved_quantity,
            i.available_quantity,
            COALESCE(s.qty_30d, 0) AS qty_30d,
            COALESCE(s.qty_60d, 0) AS qty_60d,
            COALESCE(s.qty_90d, 0) AS qty_90d,
            COALESCE(s.orders_90d, 0) AS orders_90d
        FROM inventory i
        JOIN products p ON p.id = i.product_id
        JOIN warehouses w ON w.id = i.warehouse_id
        LEFT JOIN sales s
            ON s.product_id = i.product_id
           AND s.warehouse_id = i.warehouse_id
        WHERE p.status = 'ACTIVE'
        ORDER BY p.id, w.id;
    """

    with db_connection() as conn:
        return pd.read_sql_query(sql, conn)


def create_training_target(df: pd.DataFrame) -> np.ndarray:
    weighted_30d = df["qty_30d"] * 0.60
    weighted_60d = (df["qty_60d"] / 2.0) * 0.30
    weighted_90d = (df["qty_90d"] / 3.0) * 0.10

    estimated_30d_demand = weighted_30d + weighted_60d + weighted_90d
    estimated_7d_demand = estimated_30d_demand * (7.0 / 30.0)

    return np.maximum(estimated_7d_demand, 0)


def risk_level(available: int, reorder_level: int, predicted_7d: int, days_until_stockout: float) -> str:
    if available <= reorder_level or predicted_7d >= available or days_until_stockout <= 7:
        return "HIGH"

    if days_until_stockout <= 14 or available <= reorder_level * 2:
        return "MEDIUM"

    return "LOW"


def build_forecasts(df: pd.DataFrame, predictions: np.ndarray) -> list[dict[str, Any]]:
    forecasts = []

    for index, row in df.iterrows():
        predicted_7d = max(0, int(math.ceil(float(predictions[index]))))
        predicted_daily = round(predicted_7d / 7.0, 2)

        available = int(row["available_quantity"])
        reorder_level_value = int(row["reorder_level"])

        if predicted_daily <= 0:
            estimated_days = 999.0
        else:
            estimated_days = round(available / predicted_daily, 2)

        risk = risk_level(
            available=available,
            reorder_level=reorder_level_value,
            predicted_7d=predicted_7d,
            days_until_stockout=estimated_days,
        )

        recommended_reorder = max(
            0,
            int(math.ceil((predicted_7d * 2) + reorder_level_value - available))
        )

        forecasts.append({
            "productId": int(row["product_id"]),
            "sku": row["sku"],
            "productName": row["product_name"],
            "warehouseId": int(row["warehouse_id"]),
            "warehouseCode": row["warehouse_code"],
            "warehouseName": row["warehouse_name"],
            "quantityOnHand": int(row["quantity_on_hand"]),
            "reservedQuantity": int(row["reserved_quantity"]),
            "availableQuantity": available,
            "reorderLevel": reorder_level_value,
            "predictedDemand7Days": predicted_7d,
            "predictedDailyDemand": predicted_daily,
            "estimatedDaysUntilStockout": estimated_days,
            "recommendedReorderQuantity": recommended_reorder,
            "riskLevel": risk,
        })

    forecasts.sort(
        key=lambda item: (
            {"HIGH": 0, "MEDIUM": 1, "LOW": 2}[item["riskLevel"]],
            item["estimatedDaysUntilStockout"],
            -item["predictedDemand7Days"],
        )
    )

    return forecasts


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "Inventory Demand Forecasting ML Service",
        "timestamp": datetime.utcnow().isoformat() + "Z"
    }


@app.get("/forecast", response_model=ForecastResponse)
def forecast():
    df = load_training_data()

    if df.empty:
        return ForecastResponse(
            generatedAt=datetime.utcnow().isoformat() + "Z",
            modelName="RandomForestRegressor",
            modelVersion="demo-v1",
            totalItemsScored=0,
            highRiskItems=0,
            mediumRiskItems=0,
            lowRiskItems=0,
            forecasts=[],
        )

    feature_columns = [
        "unit_price",
        "reorder_level",
        "quantity_on_hand",
        "reserved_quantity",
        "available_quantity",
        "qty_30d",
        "qty_60d",
        "qty_90d",
        "orders_90d",
    ]

    features = df[feature_columns].astype(float)
    target = create_training_target(df)

    model = RandomForestRegressor(
        n_estimators=80,
        random_state=42,
        max_depth=8,
        min_samples_leaf=2
    )

    model.fit(features, target)
    predictions = model.predict(features)

    forecasts = build_forecasts(df, predictions)

    high_risk = sum(1 for item in forecasts if item["riskLevel"] == "HIGH")
    medium_risk = sum(1 for item in forecasts if item["riskLevel"] == "MEDIUM")
    low_risk = sum(1 for item in forecasts if item["riskLevel"] == "LOW")

    return ForecastResponse(
        generatedAt=datetime.utcnow().isoformat() + "Z",
        modelName="RandomForestRegressor",
        modelVersion="demo-v1",
        totalItemsScored=len(forecasts),
        highRiskItems=high_risk,
        mediumRiskItems=medium_risk,
        lowRiskItems=low_risk,
        forecasts=forecasts[:50],
    )
