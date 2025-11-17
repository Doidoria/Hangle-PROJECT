import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"
import Layout from './pages/Layout';
import Main from './pages/main';
import Login from './pages/login';
import OAuthSuccess from './components/OAuthsuccess';
import Join from './pages/join';
import Competiton from './pages/competiton';
import Setting from './pages/setting';
import MyProfile from './pages/myProfile';
import CompetitionList from './pages/CompetitionList';
import CompetitionDetail from './pages/CompetitionDetail';
import CompetitionCreate from './pages/CompetitionCreate';
import ProtectedRoute from './components/ProtectedRoute';
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
            <Route path="/myprofile" element={<ProtectedRoute><MyProfile /></ProtectedRoute>} />
            <Route path="/setting" element={<Setting />} />
            <Route path="/competiton" element={<ProtectedRoute><Competiton /></ProtectedRoute>} />
            <Route path="/competitions" element={<ProtectedRoute><CompetitionList /></ProtectedRoute>} />
            <Route path="/competitions/new" element={<ProtectedRoute requiredRole="ROLE_MANAGER"><CompetitionCreate /></ProtectedRoute>} />
            <Route path="/competitions/:id" element={<ProtectedRoute><CompetitionDetail /></ProtectedRoute>} />
            <Route path="/FaqPage" element={<FaqPage />} />
            <Route path="/inquiry/write" element={<ProtectedRoute><InquiryWrite /></ProtectedRoute>} />
            <Route path="/myprofile/inquiries" element={<ProtectedRoute><MyInquiries /></ProtectedRoute>} />
            {/* ↓ 2. 관리자 문의 관리 페이지 라우팅 추가 (ROLE_ADMIN 필요) ↓ */}
            <Route path="/admin/inquiries" element={<ProtectedRoute requiredRole="ROLE_ADMIN"><InquiryManagement /></ProtectedRoute>} />

        </Route>

        <Route path="/login" element={<Login />}></Route>
        <Route path="/join" element={<Join />}></Route>
        <Route path="/oauth-success" element={<OAuthSuccess />} />
      </Routes>
    </BR>
    </div >
  );
}

export default App;
