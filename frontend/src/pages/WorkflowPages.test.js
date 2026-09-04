import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { Provider } from "react-redux";
import { configureStore } from "@reduxjs/toolkit";
import authReducer from "../store/authSlice";
import dashboardReducer from "../store/dashboardSlice";
import { apiRequest } from "../services/api";
import OperationsManagementPage from "./OperationsManagementPage";
import DocumentsPage from "./DocumentsPage";
import ShipmentManagementPage from "./ShipmentManagementPage";
import UsersPage from "./UsersPage";

jest.mock("../services/api", () => ({ apiRequest: jest.fn() }));

function renderWithStore(ui) {
  const store = configureStore({
    reducer: { auth: authReducer, dashboard: dashboardReducer },
    preloadedState: {
      auth: { user: { role: "ADMINISTRATEUR" }, loading: false, error: "" },
      dashboard: { data: {}, loading: false, error: "" },
    },
  });
  return render(<Provider store={store}>{ui}</Provider>);
}

beforeEach(() => {
  apiRequest.mockImplementation((path) => {
    if (path === "/operations/import") return Promise.resolve([{ id: 1, reference: "IMP-1", type: "IMPORT", status: "CREEE", supplierName: "Supplier", lines: [] }]);
    if (path === "/operations/export") return Promise.resolve([]);
    if (path === "/shipments/all") return Promise.resolve([{ id: 4, operationId: 1, operationReference: "IMP-1", carrier: "DHL", status: "LIVREE" }]);
    if (path === "/suppliers") return Promise.resolve([{ id: 2, companyName: "Supplier" }]);
    if (path === "/customers") return Promise.resolve([{ id: 3, companyName: "Customer" }]);
    if (path === "/products") return Promise.resolve([{ id: 5, name: "Product" }]);
    if (path === "/users") return Promise.resolve([{ id: 7, firstName: "Ada", lastName: "Admin", email: "ada@example.test", role: "ADMINISTRATEUR", enabled: true }]);
    if (path === "/audit") return Promise.resolve([]);
    if (path === "/document-requirements?type=IMPORT") return Promise.resolve([]);
    if (path === "/operations/1/documents") return Promise.resolve([]);
    if (path === "/operations/1/history") return Promise.resolve([{ id: 8, action: "CREATE", actorEmail: "ada@example.test", details: "Création" }]);
    return Promise.resolve([]);
  });
});

test("shows delivered shipments in administration", async () => {
  renderWithStore(<ShipmentManagementPage />);
  expect(await screen.findByText("Toutes les expéditions")).toBeInTheDocument();
  expect(await screen.findByDisplayValue("LIVREE")).toBeInTheDocument();
});

test("opens an operation history", async () => {
  renderWithStore(<OperationsManagementPage />);
  const historyButton = await screen.findByRole("button", { name: "Historique" });
  fireEvent.click(historyButton);
  expect(await screen.findByText("Historique · IMP-1")).toBeInTheDocument();
  expect(screen.getByText("Création")).toBeInTheDocument();
});

test("loads documents for the selected operation", async () => {
  renderWithStore(<DocumentsPage />);
  await screen.findByRole("option", { name: /IMP-1/ });
  const [operationSelect] = await screen.findAllByRole("combobox");
  fireEvent.change(operationSelect, { target: { value: "1" } });
  await waitFor(() => expect(apiRequest.mock.calls.some(([path]) => path === "/operations/1/documents")).toBe(true));
  expect(await screen.findByText("Aucun document déposé.")).toBeInTheDocument();
});

test("lists managed users", async () => {
  renderWithStore(<UsersPage />);
  expect(await screen.findByText("ada@example.test")).toBeInTheDocument();
});
