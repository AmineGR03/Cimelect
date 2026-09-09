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
import UsersPage from "./pages/UsersPage";
import ManagementPage from "./pages/ManagementPage";
import OperationsManagementPage from "./pages/OperationsManagementPage";
import ShipmentManagementPage from "./pages/ShipmentManagementPage";
import DocumentsPage from "./pages/DocumentsPage";
import AuditPage from "./pages/AuditPage";
import ProfilePage from "./pages/ProfilePage";
import { loadWorkspace } from "./store/dashboardSlice";
import "./App.css";

function Workspace() {
  const user = useSelector((state) => state.auth.user);
  const dispatch = useDispatch();
  const canSeeDashboard = ["ADMINISTRATEUR", "RESPONSABLE"].includes(
    user?.role,
  );
  useEffect(() => {
    if (user) {
      dispatch(
        loadWorkspace({
          canSeeDashboard,
          canSeePartners: ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"].includes(
            user.role,
          ),
        }),
      );
    }
  }, [dispatch, canSeeDashboard, user]);
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
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute
                roles={["ADMINISTRATEUR", "RESPONSABLE"]}
                redirectTo="/operations"
              >
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route path="/operations" element={<OperationsPage />} />
          <Route path="/operations/manage" element={<ProtectedRoute roles={["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"]} redirectTo="/operations"><OperationsManagementPage /></ProtectedRoute>} />
          <Route path="/shipments" element={<ShipmentsPage />} />
          <Route path="/shipments/manage" element={<ProtectedRoute roles={["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"]} redirectTo="/shipments"><ShipmentManagementPage /></ProtectedRoute>} />
          <Route path="/documents" element={<ProtectedRoute roles={["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"]} redirectTo="/operations"><DocumentsPage /></ProtectedRoute>} />
          <Route path="/audit" element={<ProtectedRoute roles={["ADMINISTRATEUR", "RESPONSABLE"]} redirectTo="/operations"><AuditPage /></ProtectedRoute>} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/partners" element={<ProtectedRoute roles={["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"]} redirectTo="/operations"><PartnersPage /></ProtectedRoute>} />
          <Route path="/partners/suppliers" element={<ProtectedRoute roles={["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"]} redirectTo="/operations"><ManagementPage type="suppliers" /></ProtectedRoute>} />
          <Route path="/partners/customers" element={<ProtectedRoute roles={["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"]} redirectTo="/operations"><ManagementPage type="customers" /></ProtectedRoute>} />
          <Route path="/products" element={<ProtectedRoute roles={["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"]} redirectTo="/operations"><ManagementPage type="products" /></ProtectedRoute>} />
          <Route path="/users" element={<ProtectedRoute roles={["ADMINISTRATEUR", "RESPONSABLE"]} redirectTo="/operations"><UsersPage /></ProtectedRoute>} />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
