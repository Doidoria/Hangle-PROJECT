// src/App.js
import './App.css';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import CompetitionList from './components/competitons/CompetitionList';
import CompetitionDetail from './components/competitons/CompetitionDetail';
import CompetitionCreate from './components/competitons/CompetitionCreate';

import './styles/base/_globals.scss';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/competitions" replace />} />
        <Route path="/competitions" element={<CompetitionList />} />
        <Route path="/competitions/:id" element={<CompetitionDetail />} />
        <Route path="/admin/competitions/new" element={<CompetitionCreate />} />
        <Route path="*" element={<div style={{ padding: 24 }}>페이지를 찾을 수 없습니다.</div>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
