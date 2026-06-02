import { createContext, useContext, useState, useEffect, useRef } from 'react'
import api from '@api/client'

const UserContext = createContext(null)

function getTokenExpiry(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000
  } catch {
    return null
  }
}

export function UserProvider({ children }) {
  const [userName, setUserName] = useState('')
  const [loggedIn, setLoggedIn] = useState(false)
  const [loading, setLoading] = useState(true)
  const timerRef = useRef(null)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const urlToken = params.get('token')
    if (urlToken) {
      localStorage.setItem('token', urlToken)
      window.history.replaceState({}, '', window.location.pathname)
    }

    api.get('/api/user/me')
      .then(({ data }) => {
        setLoggedIn(!!data.loggedIn)
        setUserName(data.name ?? '')

        if (data.loggedIn) {
          const token = localStorage.getItem('token')
          const expiry = getTokenExpiry(token)
          if (expiry) {
            const delay = expiry - Date.now() - 5 * 60 * 1000
            if (delay > 0) {
              timerRef.current = setTimeout(() => alert('세션이 5분 후 만료됩니다.'), delay)
            } else if (expiry - Date.now() > 0) {
              alert('세션이 5분 후 만료됩니다.')
            }
          }
        }
      })
      .catch(() => setLoggedIn(false))
      .finally(() => setLoading(false))

    return () => clearTimeout(timerRef.current)
  }, [])

  return (
    <UserContext.Provider value={{ userName, loggedIn, loading }}>
      {children}
    </UserContext.Provider>
  )
}

export function useUser() {
  return useContext(UserContext)
}
