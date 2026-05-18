import { createContext, useContext, useState, useEffect } from 'react'
import api from '@api/client'

const UserContext = createContext(null)

export function UserProvider({ children }) {
  const [userName, setUserName] = useState('')
  const [loggedIn, setLoggedIn] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/api/user/me')
      .then(({ data }) => {
        setLoggedIn(!!data.loggedIn)
        setUserName(data.name ?? '')
      })
      .catch(() => setLoggedIn(false))
      .finally(() => setLoading(false))
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
