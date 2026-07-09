import { Bot, Send, User } from "lucide-react";
import { FormEvent, useState } from "react";
import { askChatbot } from "../lib/api";

type Message = {
  role: "user" | "assistant";
  content: string;
};

const suggestedQuestions = [
  "Which products are low in stock?",
  "Summarize recent order activity.",
  "Which warehouse has the most available inventory?",
  "What should the operations team focus on today?",
  "Which products are selling the most?"
];

export default function ChatbotPage() {
  const [messages, setMessages] = useState<Message[]>([
    {
      role: "assistant",
      content:
        "Hi, I am your inventory operations assistant. Ask me about low stock, orders, warehouses, top products, or operational priorities."
    }
  ]);
  const [input, setInput] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function sendMessage(message: string) {
    if (!message.trim()) {
      return;
    }

    setError("");
    setLoading(true);

    const userMessage: Message = {
      role: "user",
      content: message
    };

    setMessages((current) => [...current, userMessage]);
    setInput("");

    try {
      const response = await askChatbot(message);

      setMessages((current) => [
        ...current,
        {
          role: "assistant",
          content: response.answer
        }
      ]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Chatbot request failed");
    } finally {
      setLoading(false);
    }
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    sendMessage(input);
  }

  return (
    <div>
      <div className="mb-8">
        <p className="text-sm uppercase tracking-[0.3em] text-cyan-300">AI Assistant</p>
        <h2 className="mt-3 text-3xl font-bold text-white">Operations Chatbot</h2>
        <p className="mt-2 text-slate-400">
          Ask operational questions about products, inventory, orders, warehouses, analytics, and risk.
        </p>
      </div>

      {error && <div className="mb-6 rounded-xl bg-red-500/10 p-4 text-red-300">{error}</div>}

      <div className="grid gap-6 xl:grid-cols-[1fr_320px]">
        <div className="card flex min-h-[650px] flex-col overflow-hidden">
          <div className="border-b border-slate-800 p-5">
            <h3 className="text-lg font-semibold text-white">Chat</h3>
            <p className="mt-1 text-sm text-slate-400">
              The backend provides controlled business context before calling OpenAI.
            </p>
          </div>

          <div className="flex-1 space-y-5 overflow-y-auto p-5">
            {messages.map((message, index) => (
              <div
                key={index}
                className={`flex gap-3 ${
                  message.role === "user" ? "justify-end" : "justify-start"
                }`}
              >
                {message.role === "assistant" && (
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-cyan-400 text-slate-950">
                    <Bot size={20} />
                  </div>
                )}

                <div
                  className={`max-w-[80%] whitespace-pre-wrap rounded-2xl px-5 py-4 text-sm leading-6 ${
                    message.role === "user"
                      ? "bg-cyan-400 text-slate-950"
                      : "bg-slate-800 text-slate-100"
                  }`}
                >
                  {message.content}
                </div>

                {message.role === "user" && (
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-slate-800 text-cyan-300">
                    <User size={20} />
                  </div>
                )}
              </div>
            ))}

            {loading && (
              <div className="flex gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-cyan-400 text-slate-950">
                  <Bot size={20} />
                </div>
                <div className="rounded-2xl bg-slate-800 px-5 py-4 text-sm text-slate-300">
                  Thinking...
                </div>
              </div>
            )}
          </div>

          <form onSubmit={handleSubmit} className="border-t border-slate-800 p-5">
            <div className="flex gap-3">
              <input
                className="input"
                placeholder="Ask about low stock, orders, warehouses, top products..."
                value={input}
                onChange={(event) => setInput(event.target.value)}
              />
              <button disabled={loading} className="btn-primary flex items-center gap-2">
                <Send size={16} />
                Send
              </button>
            </div>
          </form>
        </div>

        <div className="space-y-5">
          <div className="card p-5">
            <h3 className="text-lg font-semibold text-white">Suggested Questions</h3>
            <div className="mt-4 space-y-3">
              {suggestedQuestions.map((question) => (
                <button
                  key={question}
                  onClick={() => sendMessage(question)}
                  className="w-full rounded-xl border border-slate-800 px-4 py-3 text-left text-sm text-slate-300 transition hover:border-cyan-400 hover:text-cyan-300"
                >
                  {question}
                </button>
              ))}
            </div>
          </div>

          <div className="card p-5">
            <h3 className="text-lg font-semibold text-white">Safety Design</h3>
            <div className="mt-4 space-y-3 text-sm text-slate-400">
              <p>OpenAI never receives database credentials.</p>
              <p>The frontend never receives the OpenAI API key.</p>
              <p>The backend sends only approved operational summaries.</p>
              <p>The chatbot is instructed not to invent unavailable data.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
