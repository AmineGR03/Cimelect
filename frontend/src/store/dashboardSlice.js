import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../services/api";

export const loadWorkspace = createAsyncThunk(
  "dashboard/loadWorkspace",
  async ({ canSeeDashboard, canSeePartners }) => {
    const requests = [
      apiRequest("/operations/import"),
      apiRequest("/operations/export"),
      apiRequest("/shipments/in-progress"),
    ];
    if (canSeePartners) {
      requests.push(apiRequest("/suppliers"), apiRequest("/customers"));
    }
    if (canSeeDashboard) requests.push(apiRequest("/dashboard"));
    const results = await Promise.all(requests);
    const imports = results[0];
    const exports = results[1];
    const shipments = results[2];
    const partnersStart = 3;
    const suppliers = canSeePartners ? results[partnersStart] : [];
    const customers = canSeePartners ? results[partnersStart + 1] : [];
    const dashboard = canSeeDashboard
      ? results[canSeePartners ? partnersStart + 2 : partnersStart]
      : undefined;
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
