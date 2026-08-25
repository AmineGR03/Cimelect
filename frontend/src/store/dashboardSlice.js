import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../services/api";

export const loadWorkspace = createAsyncThunk(
  "dashboard/loadWorkspace",
  async (canSeeDashboard) => {
    const requests = [
      apiRequest("/operations/import"),
      apiRequest("/operations/export"),
      apiRequest("/shipments/in-progress"),
      apiRequest("/suppliers"),
      apiRequest("/customers"),
    ];
    if (canSeeDashboard) requests.push(apiRequest("/dashboard"));
    const [imports, exports, shipments, suppliers, customers, dashboard] =
      await Promise.all(requests);
    return {
      operations: [...imports, ...exports],
      shipments,
      partners: [
        ...suppliers.map((item) => ({ ...item, kind: "Fournisseur" })),
        ...customers.map((item) => ({ ...item, kind: "Client" })),
      ],
      dashboard,
    };
  },
);

export default createSlice({
  name: "dashboard",
  initialState: { data: {}, loading: false, error: "" },
  reducers: {},
  extraReducers: (builder) =>
    builder
      .addCase(loadWorkspace.pending, (state) => {
        state.loading = true;
        state.error = "";
      })
      .addCase(loadWorkspace.fulfilled, (state, action) => {
        state.loading = false;
        state.data = action.payload;
      })
      .addCase(loadWorkspace.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
      }),
}).reducer;
