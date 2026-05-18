import { createBrowserRouter } from "react-router-dom";
import { Layout } from "./components/Layout";
import { Dashboard } from "./components/Dashboard";
import { CreateTravelPlan } from "./components/CreateTravelPlan";
import { RouteOptimization } from "./components/RouteOptimization";
import { ActivityScheduling } from "./components/ActivityScheduling";
import { AttractionRecommendations } from "./components/AttractionRecommendations";
import { WeatherForecast } from "./components/WeatherForecast";
import { BudgetManagement } from "./components/BudgetManagement";
import { CollaborativePlanning } from "./components/CollaborativePlanning";
import { ActivityVoting } from "./components/ActivityVoting";
import { LoginPage, RegisterPage } from "./components/AuthPages";
import { ReservationManagement } from "./components/ReservationManagement";
import { RequireAuth } from "./components/RequireAuth";
import { OfflineAccess } from "./components/OfflineAccess";
import { NotificationsReminders } from "./components/NotificationsReminders";
import { ScheduleLoadEvaluation } from "./components/ScheduleLoadEvaluation";
import { LocalRecommendations } from "./components/LocalRecommendations";
import { TravelPlanSharing } from "./components/TravelPlanSharing";
import { WaitingTimeAnalysis } from "./components/WaitingTimeAnalysis";

export const router = createBrowserRouter([
  { path: "/login", Component: LoginPage },
  { path: "/register", Component: RegisterPage },
  {
    Component: RequireAuth,
    children: [
      {
        path: "/",
        Component: Layout,
        children: [
          { index: true, Component: Dashboard },
          { path: "create-plan", Component: CreateTravelPlan },
          { path: "planning", Component: CreateTravelPlan },
          { path: "route-optimization", Component: RouteOptimization },
          { path: "activity-scheduling", Component: ActivityScheduling },
          { path: "attractions", Component: AttractionRecommendations },
          { path: "weather", Component: WeatherForecast },
          { path: "budget", Component: BudgetManagement },
          { path: "collaborative", Component: CollaborativePlanning },
          { path: "voting", Component: ActivityVoting },
          { path: "reservations", Component: ReservationManagement },
          { path: "offline", Component: OfflineAccess },
          { path: "notifications", Component: NotificationsReminders },
          { path: "schedule-load", Component: ScheduleLoadEvaluation },
          { path: "local-recommendations", Component: LocalRecommendations },
          { path: "share", Component: TravelPlanSharing },
          { path: "waiting-times", Component: WaitingTimeAnalysis },
        ],
      },
    ],
  },
]);
