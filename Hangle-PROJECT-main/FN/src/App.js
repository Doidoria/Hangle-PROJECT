import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"

import CompetitionList from './pages/CompetitionList';
import CompetitionDetail from './pages/CompetitionDetail';
import CompetitionCreate from './pages/CompetitionCreate';

import Main from './pages/main';
import Login from './pages/login';
import Join from './pages/join';
import Competiton from './pages/competiton';


function App() {
  return (
    <div className="App">
      <BR>
        <Routes>
          <Route path="/" element={<Main />}></Route>
          <Route path="/login" element={<Login />}></Route>
          <Route path="/join" element={<Join />}></Route>
          {/* <Route path="/competiton" element={<competiton />}></Route> */}
          <Route path="/CompetitionList" element={<CompetitionList />}></Route>
          <Route path="/CompetitionDetail" element={<CompetitionDetail />}></Route>
          <Route path="/CompetitionCreate" element={<CompetitionCreate />}></Route>
        </Routes>
      </BR>
    </div>
  );
}

export default App;
