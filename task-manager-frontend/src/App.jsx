import { useEffect, useState } from 'react'
import Login from './Login'

const API_URL = import.meta.env.VITE_API_URL

function App() {
  const [token, setToken] = useState(null)
  const [tasks, setTasks] = useState([])
  const [title, setTitle] = useState('')

  // Every request goes through here: adds the token, and logs out on 401
  const apiFetch = (path, options = {}) =>
    fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`
      }
    }).then(res => {
      if (res.status === 401) {
        setToken(null)
        throw new Error('Session expired')
      }
      return res
    })

  const fetchTasks = () => {
    apiFetch('/tasks')
      .then(res => res.json())
      .then(data => setTasks(data))
      .catch(err => console.error(err))
  }

  useEffect(() => {
    if (token) fetchTasks()
    else setTasks([])
  }, [token])

  const handleSubmit = (e) => {
    e.preventDefault()
    apiFetch('/tasks', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title })
    })
      .then(res => res.json())
      .then(() => {
        setTitle('')
        fetchTasks()
      })
      .catch(err => console.error(err))
  }

  const handleComplete = (id) => {
    apiFetch(`/tasks/${id}/complete`, { method: 'PUT' })
      .then(() => fetchTasks())
      .catch(err => console.error(err))
  }

  const handleUndo = (id) => {
    apiFetch(`/tasks/${id}/undo`, { method: 'PUT' })
      .then(() => fetchTasks())
      .catch(err => console.error(err))
  }

  const handleDelete = (id) => {
    apiFetch(`/tasks/${id}`, { method: 'DELETE' })
      .then(() => fetchTasks())
      .catch(err => console.error(err))
  }

  if (!token) {
    return <Login onLogin={setToken} />
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-md p-6 w-full max-w-md">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold text-gray-800">Task Manager</h1>
          <button
            onClick={() => setToken(null)}
            className="text-sm text-gray-500 hover:text-gray-800"
          >
            Log out
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex gap-2 mb-6">
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="New task title"
            className="flex-1 border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
          <button
            type="submit"
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            Add
          </button>
        </form>

        <ul className="space-y-2">
          {tasks.map(task => (
            <li
              key={task.id}
              className="flex items-center justify-between border border-gray-200 rounded px-3 py-2"
            >
              <span className={task.status === 'DONE' ? 'line-through text-gray-400' : 'text-gray-800'}>
                {task.title}
              </span>
              <div className="flex gap-2">
                {task.status !== 'DONE' ? (
                  <button
                    onClick={() => handleComplete(task.id)}
                    className="text-sm bg-green-500 text-white px-2 py-1 rounded hover:bg-green-600"
                  >
                    Complete
                  </button>
                ) : (
                  <button
                    onClick={() => handleUndo(task.id)}
                    className="text-sm bg-yellow-500 text-white px-2 py-1 rounded hover:bg-yellow-600"
                  >
                    Undo
                  </button>
                )}
                <button
                  onClick={() => handleDelete(task.id)}
                  className="text-sm bg-red-500 text-white px-2 py-1 rounded hover:bg-red-600"
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}

export default App