import logo from './logo.svg';
import './App.css';

import { BrowserRouter as BR, Routes, Route } from "react-router-dom"
import Main from './pages/main';
import Login from './pages/login';
import Join from './pages/join';
import Competiton from './pages/competiton';
import Setting from './pages/setting';
import Layout from './pages/Layout';
import MyProfile from './pages/myProfile';

function App() {
  return (
    <div className="App">
      <BR>
        <Routes>
          <Route element={<Layout />}>
          <Route path="/" element={<Main />}></Route>
          <Route path="/competiton" element={<Competiton />}></Route>
          <Route path="/setting" element={<Setting />}></Route>
          <Route path="/myProfile" element={<MyProfile />}></Route>
          </Route>

          <Route path="/login" element={<Login />}></Route>
          <Route path="/join" element={<Join />}></Route>
        </Routes>
      </BR>
    </div>
  );
}

export default App;
