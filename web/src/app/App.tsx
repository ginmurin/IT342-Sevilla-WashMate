import { Component, ReactNode } from "react";
import { RouterProvider } from "react-router";
import { AuthProvider } from "./contexts/AuthContext";
import { router } from "./routes";

class ErrorBoundary extends Component<{ children: ReactNode }, { error: Error | null }> {
  state = { error: null };
  static getDerivedStateFromError(error: Error) { return { error }; }
  render() {
    if (this.state.error) {
      return (
        <div style={{ padding: 32, fontFamily: "monospace", background: "#fee", color: "#900" }}>
          <h2>Runtime Error</h2>
          <pre style={{ whiteSpace: "pre-wrap" }}>{(this.state.error as Error).message}</pre>
          <pre style={{ whiteSpace: "pre-wrap", fontSize: 12 }}>{(this.state.error as Error).stack}</pre>
        </div>
      );
    }
    return this.props.children;
  }
}

export default function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </ErrorBoundary>
  );
}
