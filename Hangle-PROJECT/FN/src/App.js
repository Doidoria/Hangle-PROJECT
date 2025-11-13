import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"
import Layout from './pages/Layout';
import Main from './pages/main';
import Login from './pages/login';
import OAuthSuccess from './components/OAuthsuccess';
import Join from './pages/join';
import Competition from './pages/competition';
import Setting from './pages/setting';
import MyProfile from './pages/myProfile';
import CompetitionList from './pages/CompetitionList';
import CompetitionDetail from './pages/CompetitionDetail';
import CompetitionCreate from './pages/CompetitionCreate';
import ProtectedRoute from './components/ProtectedRoute';
import Leaderboard from './pages/leaderboard';

function App() {
  return (
    <div className="App">
      <BR>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Main />} />
            <Route path="/myprofile" element={<ProtectedRoute><MyProfile /></ProtectedRoute>} />
            <Route path="/setting" element={<Setting />} />
            <Route path="/competition" element={<ProtectedRoute><Competition /></ProtectedRoute>} />
            <Route path="/competitions" element={<ProtectedRoute><CompetitionList /></ProtectedRoute>} />
            <Route path="/competitions/new" element={<ProtectedRoute><CompetitionCreate /></ProtectedRoute>} />
            <Route path="/competitions/:id" element={<ProtectedRoute><CompetitionDetail /></ProtectedRoute>} />

            <Route path="/leaderboard" element={<Leaderboard/>}></Route>
          </Route>

          <Route path="/login" element={<Login />} />
          <Route path="/join" element={<Join />} />
          <Route path="/oauth-success" element={<OAuthSuccess />} />
        </Routes>
      </BR>
    </div>
  );
}

export default App;
