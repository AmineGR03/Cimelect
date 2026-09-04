import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { apiRequest } from "../services/api";

const savedUser = JSON.parse(localStorage.getItem("cimelect_user") || "null");
export const login = createAsyncThunk("auth/login", (credentials) =>
  apiRequest("/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  }),
);
export const logout = createAsyncThunk("auth/logout", async () => {
  try {
    await apiRequest("/auth/logout", { method: "POST" });
  } finally {
    localStorage.clear();
  }
});

const authSlice = createSlice({
  name: "auth",
  initialState: { user: savedUser, loading: false, error: "" },
  reducers: {
    setUser: (state, action) => {
      state.user = { ...state.user, ...action.payload };
      localStorage.setItem("cimelect_user", JSON.stringify(state.user));
    },
  },
  extraReducers: (builder) =>
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = "";
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        localStorage.setItem("cimelect_token", action.payload.token);
        localStorage.setItem("cimelect_user", JSON.stringify(action.payload));
      })
      .addCase(login.rejected, (state) => {
        state.loading = false;
        state.error = "Identifiants invalides ou serveur indisponible.";
      })
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
      }),
});
export const { setUser } = authSlice.actions;
export default authSlice.reducer;
