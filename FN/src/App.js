import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"
import Layout from './pages/Layout';
import Main from './pages/main';
import Login from './pages/login';
import OAuthSuccess from './components/OAuthsuccess';
import ProtectedRoute from './components/ProtectedRoute';
import Join from './pages/join';
import Setting from './pages/setting';
import MyProfile from './pages/myProfile';
import Competition from './pages/Competition';
import CompetitionList from './pages/CompetitionList';
import CompetitionDetail from './pages/CompetitionDetail';
import CompetitionCreate from './pages/CompetitionCreate';
import Leaderboard from './pages/leaderboard';
import InquiryWrite from './pages/InquiryWrite';
import FaqPage from './pages/FaqPage';
import MyInquiries from './pages/MyInquiries';
import InquiryManagement from './pages/InquiryManagement';

function App() {
  return (
    <div className="App">
      <BR>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Main />} />
            <Route path="/myprofile" element={<ProtectedRoute><MyProfile/></ProtectedRoute>} />
            <Route path="/setting" element={<Setting />} />
            <Route path="/FaqPage" element={<FaqPage />} />
            <Route path="/inquiry/write" element={<ProtectedRoute><InquiryWrite /></ProtectedRoute>} />
            <Route path="/myprofile/inquiries" element={<ProtectedRoute><MyInquiries /></ProtectedRoute>} />
            <Route path="/admin/inquiries" element={<ProtectedRoute><InquiryManagement /></ProtectedRoute>} />
            <Route path="/competitions/user" element={<Competition />} />
            <Route path="/competitions/:id" element={<CompetitionDetail />} />
            <Route path="/competitions/list" element={<ProtectedRoute><CompetitionList /></ProtectedRoute>} />
            <Route path="/competitions/new" element={<ProtectedRoute><CompetitionCreate /></ProtectedRoute>} />
            <Route path="/leaderboard" element={<Leaderboard />} />
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
