import { useEffect } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import AppLayout from "./layouts/AppLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import OperationsPage from "./pages/OperationsPage";
import ShipmentsPage from "./pages/ShipmentsPage";
import PartnersPage from "./pages/PartnersPage";
import { loadWorkspace } from "./store/dashboardSlice";
import "./App.css";

function Workspace() {
  const user = useSelector((state) => state.auth.user);
  const dispatch = useDispatch();
  const canSeeDashboard = ["ADMINISTRATEUR", "RESPONSABLE"].includes(user.role);
  useEffect(() => {
    dispatch(loadWorkspace(canSeeDashboard));
  }, [dispatch, canSeeDashboard]);
  return <AppLayout />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          element={
            <ProtectedRoute>
              <Workspace />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/operations" element={<OperationsPage />} />
          <Route path="/shipments" element={<ShipmentsPage />} />
          <Route path="/partners" element={<PartnersPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
