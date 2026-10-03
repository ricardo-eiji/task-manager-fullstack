import { useEffect, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL
  // This is associated with the .env.development (if we use `npm run dev` in task-manager-frontend directory)
  // Or it's associated with docker-compose.yaml (If we use `docker compose up` at root)

function App() {
  const [tasks, setTasks] = useState([])
  const [title, setTitle] = useState('')

  const authHeader = 'Basic ' + btoa('admin:admin123')

  const fetchTasks = () => {
    fetch(`${API_URL}/tasks`, {
      headers: { Authorization: authHeader }
    })
      .then(res => res.json())
      .then(data => setTasks(data))
      .catch(err => console.error(err))
  }

  useEffect(() => {
    fetchTasks()
  }, [])

  const handleSubmit = (e) => {
    e.preventDefault()
    fetch(`${API_URL}/tasks`, {
      method: 'POST',
      headers: {
        Authorization: authHeader,
        'Content-Type': 'application/json'
      },
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
    fetch(`${API_URL}/tasks/${id}/complete`, {
      method: 'PUT',
      headers: { Authorization: authHeader }
    })
      .then(() => fetchTasks())
      .catch(err => console.error(err))
  }

  const handleUndo = (id) => {
    fetch(`${API_URL}/tasks/${id}/undo`, {
      method: 'PUT',
      headers: { Authorization: authHeader }
    })
      .then(() => fetchTasks())
      .catch(err => console.error(err))
  }

  const handleDelete = (id) => {
    fetch(`${API_URL}/tasks/${id}`, {
      method: 'DELETE',
      headers: { Authorization: authHeader }
    })
      .then(() => fetchTasks())
      .catch(err => console.error(err))
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-md p-6 w-full max-w-md">
        <h1 className="text-2xl font-bold text-gray-800 mb-4">Task Manager</h1>

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