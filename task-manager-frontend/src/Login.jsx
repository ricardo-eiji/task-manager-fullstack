import { useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL

// POST helper: returns the JSON body, or throws an Error with the server's message
async function post(path, body) {
  const res = await fetch(`${API_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    throw new Error(data.error || Object.values(data)[0] || 'Something went wrong')
  }
  return data
}

function Login({ onLogin }) {
  const [mode, setMode] = useState('login') // 'login' or 'register'
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const isRegister = mode === 'register'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      if (isRegister) {
        await post('/auth/register', { username, password })
      }
      const data = await post('/auth/login', { username, password })
      onLogin(data.token)
    } catch (err) {
      setError(err.message)
    }
  }

  const switchMode = () => {
    setMode(isRegister ? 'login' : 'register')
    setError('')
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6 w-full max-w-sm space-y-3">
        <h1 className="text-2xl font-bold text-gray-800">
          {isRegister ? 'Create account' : 'Login'}
        </h1>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Username"
          required
          minLength={3}
          className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          required
          minLength={isRegister ? 8 : undefined}
          className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
        {error && <p className="text-sm text-red-500">{error}</p>}
        <button type="submit" className="w-full bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">
          {isRegister ? 'Sign up' : 'Log in'}
        </button>
        <button
          type="button"
          onClick={switchMode}
          className="w-full text-sm text-gray-500 hover:text-gray-800"
        >
          {isRegister ? 'Already have an account? Log in' : "No account yet? Sign up"}
        </button>
      </form>
    </div>
  )
}

export default Login