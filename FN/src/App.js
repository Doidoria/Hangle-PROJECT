import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"
import Layout from './pages/Layout';
import Main from './pages/main';
import Login from './pages/login';
import OAuthSuccess from './components/OAuthsuccess';
import Join from './pages/join';
import Setting from './pages/setting';
import MyProfile from './pages/myProfile';
import CompetitionList from './pages/CompetitionList';
import CompetitionDetail from './pages/CompetitionDetail';
import CompetitionCreate from './pages/CompetitionCreate';
import ProtectedRoute from './components/ProtectedRoute';
import Competition from './pages/Competition';

function App() {
  return (
    <div className="App">
      <BR>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Main />} />
            <Route path="/myprofile" element={<ProtectedRoute><MyProfile /></ProtectedRoute>} />
            <Route path="/setting" element={<Setting />} />
            <Route path="/competitions" element={<Competition />} />
            <Route path="/competitions/:id" element={<CompetitionDetail />} />
            <Route path="/competitions/new" element={<ProtectedRoute requiredRole="MANAGER"><CompetitionCreate /></ProtectedRoute>} />
            <Route path="/manager/competitions" element={<ProtectedRoute requiredRole="MANAGER"><CompetitionList /></ProtectedRoute>} />
          </Route>

          <Route path="/login" element={<Login />}></Route>
          <Route path="/join" element={<Join />}></Route>
          <Route path="/oauth-success" element={<OAuthSuccess />} />
        </Routes>
      </BR>
    </div>
  );
}

export default App;
