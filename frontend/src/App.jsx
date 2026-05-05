import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Home from './pages/home/Home.jsx'
import Login from './pages/auth/Login.jsx'
import Signup from './pages/auth/Signup.jsx'
import TeamSelect from './pages/auth/TeamSelect.jsx'
import Dashboard from './pages/dashboard/Dashboard.jsx'
import Result from './pages/result/Result.jsx'
import Evaluate from './pages/evaluate/Evaluate.jsx'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/team-select" element={<TeamSelect />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/result" element={<Result />} />
        <Route path="/evaluate/:id" element={<Evaluate />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
