import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"
import Layout from './pages/Layout';
import Main from './pages/main';
import Login from './pages/login';
import Join from './pages/join';
import Competition from './pages/competition';
import Setting from './pages/setting';
import MyProfile from './pages/myProfile';
import CompetitionList from './pages/CompetitionList';
import CompetitionDetail from './pages/CompetitionDetail';
import CompetitionCreate from './pages/CompetitionCreate';

function App() {
  return (
    <div className="App">
      <BR>
        <Routes>
          <Route element={<Layout />}> {/*추가 및 변경 */}
            <Route path="/" element={<Main />} />
            <Route path="/competition" element={<Competition />} />
            <Route path="/myprofile" element={<MyProfile />} />
            <Route path="/setting" element={<Setting />} />
            <Route path="/competitions" element={<CompetitionList />} />
            <Route path="/competitions/new" element={<CompetitionCreate />} />
            <Route path="/competitions/:id" element={<CompetitionDetail />} />
          </Route>

          <Route path="/login" element={<Login />}></Route>
          <Route path="/join" element={<Join />}></Route>
        </Routes>
      </BR>
    </div>
  );
}

export default App;App.js