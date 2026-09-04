import { render, screen } from "@testing-library/react";
import { Provider } from "react-redux";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { configureStore } from "@reduxjs/toolkit";
import authReducer from "../store/authSlice";
import dashboardReducer from "../store/dashboardSlice";
import ProtectedRoute from "./ProtectedRoute";

test("redirects an agent away from the dashboard", () => {
  const store = configureStore({
    reducer: { auth: authReducer, dashboard: dashboardReducer },
    preloadedState: {
      auth: { user: { role: "AGENT_IMPORT_EXPORT" }, loading: false, error: "" },
      dashboard: { data: {}, loading: false, error: "" },
    },
  });

  render(
    <Provider store={store}>
      <MemoryRouter initialEntries={["/dashboard"]}>
        <Routes>
          <Route
            path="/dashboard"
            element={<ProtectedRoute roles={["ADMINISTRATEUR"]} redirectTo="/operations"><div>Dashboard</div></ProtectedRoute>}
          />
          <Route path="/operations" element={<div>Operations</div>} />
        </Routes>
      </MemoryRouter>
    </Provider>,
  );

  expect(screen.getByText("Operations")).toBeInTheDocument();
  expect(screen.queryByText("Dashboard")).not.toBeInTheDocument();
});
