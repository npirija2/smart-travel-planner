import { RouterProvider } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { PlanProvider } from "./context/PlanContext";
import { router } from "./routes";

export default function App() {
  return (
    <AuthProvider>
      <PlanProvider>
        <RouterProvider router={router} />
      </PlanProvider>
    </AuthProvider>
  );
}