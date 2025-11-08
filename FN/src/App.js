import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"
import Main from './pages/main';
import Login from './pages/login';
import Join from './pages/join';
import Competiton from './pages/competiton';
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
          <Route path="/" element={<Main />}></Route>
          <Route path="/login" element={<Login />}></Route>
          <Route path="/join" element={<Join />}></Route>
          <Route path="/competiton" element={<Competiton />}></Route>
          <Route path="/myprofile" element={<MyProfile />}></Route>
          <Route path="/setting" element={<Setting />}></Route>
          
          <Route path="/competitions" element={<CompetitionList />} />
          <Route path="/competitions/new" element={<CompetitionCreate />} />
          <Route path="/competitions/:id" element={<CompetitionDetail />} />
        </Routes>
      </BR>
    </div>
  );
}

export default App;
