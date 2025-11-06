import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"

import CompetitionList from './pages/CompetitionList';
import CompetitionDetail from './pages/CompetitionDetail';
import CompetitionCreate from './pages/CompetitionCreate';

import Main from './pages/main';
import Login from './pages/login';
import Join from './pages/join';
// import competition from './pages/competition';


function App() {
  return (
    <div className="App">
      <BR>
        <Routes>
          <Route path="/" element={<Main />}></Route>
          <Route path="/login" element={<Login />}></Route>
          <Route path="/join" element={<Join />}></Route>
          {/* <Route path="/competiton" element={<competiton />}></Route> */}
          {/* 대회 페이지 */}
          <Route path="/competitions" element={<CompetitionList />} />
          <Route path="/competitions/new" element={<CompetitionCreate />} />
          <Route path="/competitions/:id" element={<CompetitionDetail />} />
        </Routes>
      </BR>
    </div>
  );
}

export default App;
