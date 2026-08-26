import { BrowserRouter, Routes, Route, useNavigate } from 'react-router-dom'
import { useEffect } from 'react'
import { UserProvider, useUser } from './context/UserContext.jsx'
import { isHandling401 } from './api/client.js'
import Home from './pages/home/Home.jsx'
import Login from './pages/auth/Login.jsx'
import Signup from './pages/auth/Signup.jsx'
import TeamSelect from './pages/auth/TeamSelect.jsx'
import TeamMembersOverview from './pages/team-members/Overview.jsx'
import Result from './pages/result/Result.jsx'
import Evaluate from './pages/evaluate/Evaluate.jsx'
import NotFound from './pages/notfound/NotFound.jsx'

function ProtectedRoute({ children }) {
  const { loggedIn, loading } = useUser()
  const navigate = useNavigate()

  useEffect(() => {
    if (!loading && !loggedIn) {
      if (!isHandling401) {
        alert('로그인이 필요한 서비스입니다.')
      }
      navigate('/login', { replace: true })
    }
  }, [loading, loggedIn, navigate])

  if (loading || !loggedIn) return null

  return children
}

function App() {
  return (
    <UserProvider>
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/team-select" element={<TeamSelect />} />
        <Route path="/team-members/overview" element={<ProtectedRoute><TeamMembersOverview /></ProtectedRoute>} />
        <Route path="/result" element={<ProtectedRoute><Result /></ProtectedRoute>} />
        <Route path="/evaluate/:id" element={<ProtectedRoute><Evaluate /></ProtectedRoute>} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
    </UserProvider>
  )
}

export default App
