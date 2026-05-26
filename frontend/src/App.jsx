import { useEffect, useState } from 'react'
import logo from './assets/react.svg'
import './App.css'

function App() {
  const [todos, setTodos] = useState([]);
  const [taskdescription, setTaskdescription] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [sortMode, setSortMode] = useState("az");
  const [editingTask, setEditingTask] = useState(null);
  const [editValue, setEditValue] = useState("");

  const fetchTasks = () => {
    fetch("http://localhost:8080/")
      .then(response => response.json())
      .then(data => {
        setTodos(data);
      });
  };

  /** Is called when the html form is submitted. It sends a POST request to the API endpoint '/tasks' and updates the component's state with the new todo.
  ** In this case a new taskdecription is added to the actual list on the server.
  */
  const handleSubmit = event => {
    event.preventDefault();
    if (taskdescription.trim().length === 0) {
      return;
    }
    console.log("Sending task description to Spring-Server: "+taskdescription);
    fetch("http://localhost:8080/tasks", {  // API endpoint (the complete URL!) to save a taskdescription
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ taskdescription: taskdescription }) // both 'taskdescription' are identical to Task-Class attribute in Spring
    })
    .then(response => {
      console.log("Receiving answer after sending to Spring-Server: ");
      console.log(response);
      setTaskdescription("");             // clear input field, preparing it for the next input
      fetchTasks();
    })
    .catch(error => console.log(error))
  }

   /** Is called when ever the html input field value below changes to update the component's state.
  ** This is, because the submit should not take the field value directly.
  ** The task property in the state is used to store the current value of the input field as the user types into it.
  ** This is necessary because React operates on the principle of state and props, which means that a component's state
  ** determines the component's behavior and render.
  ** If we used the value directly from the HTML form field, we wouldn't be able to update the component's state and react to changes in the input field.
  */
  const handleChange = event => {
    setTaskdescription(event.target.value);
  }


  /** Is called when the component is mounted (after any refresh or F5).
  ** It updates the component's state with the fetched todos from the API Endpoint '/'.
  */
  useEffect(() => {
    fetchTasks();
  }, []);


 /** Is called when the Done-Button is pressed. It sends a POST request to the API endpoint '/delete' and updates the component's state with the new todo.
  ** In this case if the task with the unique taskdescription is found on the server, it will be removed from the list.
  */
  const handleDelete = (event, taskdescription) => {
    console.log("Sending task description to delete on Spring-Server: "+taskdescription);
    fetch(`http://localhost:8080/delete`, { // API endpoint (the complete URL!) to delete an existing taskdescription in the list
      method: "POST",
      body: JSON.stringify({ taskdescription: taskdescription }),
      headers: {
        "Content-Type": "application/json"
      }
    })
    .then(response => {
      console.log("Receiving answer after deleting on Spring-Server: ");
      console.log(response);
      fetchTasks();
    })
    .catch(error => console.log(error))
  }

  const handleEditStart = (taskdescription) => {
    setEditingTask(taskdescription);
    setEditValue(taskdescription);
  }

  const handleEditCancel = () => {
    setEditingTask(null);
    setEditValue("");
  }

  const handleUpdate = (event, taskdescription) => {
    event.preventDefault();
    if (editValue.trim().length === 0) {
      return;
    }
    console.log("Sending update to Spring-Server: "+taskdescription+" -> "+editValue);
    fetch("http://localhost:8080/update", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        taskdescription: taskdescription,
        newTaskdescription: editValue
      })
    })
    .then(response => {
      console.log("Receiving answer after updating on Spring-Server: ");
      console.log(response);
      setEditingTask(null);
      setEditValue("");
      fetchTasks();
    })
    .catch(error => console.log(error))
  }

  const handleToggleDone = (taskdescription, done) => {
    setTodos(prevTodos =>
      prevTodos.map(todo =>
        todo.taskdescription === taskdescription
          ? { ...todo, done: done }
          : todo
      )
    );
    fetch("http://localhost:8080/toggle-done", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ taskdescription: taskdescription, done: done })
    })
    .then(response => {
      console.log("Receiving answer after toggle on Spring-Server: ");
      console.log(response);
      fetchTasks();
    })
    .catch(error => console.log(error))
  }

  const getVisibleTasks = () => {
    const searchLower = searchQuery.trim().toLowerCase();
    const filtered = todos.filter(todo => {
      const text = todo.taskdescription.toLowerCase();
      if (searchLower.length > 0 && !text.includes(searchLower)) {
        return false;
      }
      if (statusFilter === "open" && todo.done) {
        return false;
      }
      if (statusFilter === "done" && !todo.done) {
        return false;
      }
      return true;
    });

    if (sortMode === "za") {
      return filtered.slice().sort((a, b) => b.taskdescription.localeCompare(a.taskdescription));
    }
    return filtered.slice().sort((a, b) => a.taskdescription.localeCompare(b.taskdescription));
  }

  /**
   * render all task lines
   * @param {*} todos : Task list
   * @returns html code snippet
  */
  const renderTasks = (todos) => {
    return (
      <ul className="todo-list">
        {todos.map((todo, index) => (
          <li key={todo.taskdescription} className={todo.done ? "todo-item done" : "todo-item"}>
            {editingTask === todo.taskdescription ? (
              <form className="todo-edit" onSubmit={(event) => handleUpdate(event, todo.taskdescription)}>
                <input
                  type="text"
                  value={editValue}
                  onChange={(event) => setEditValue(event.target.value)}
                />
                <button type="submit">Speichern</button>
                <button type="button" onClick={handleEditCancel}>Abbrechen</button>
              </form>
            ) : (
              <>
                <div className="todo-main">
                  <label className="todo-check">
                    <input
                      type="checkbox"
                      checked={!!todo.done}
                      onChange={(event) => handleToggleDone(todo.taskdescription, event.target.checked)}
                    />
                    <span className="todo-text">{"Aufgabe " + (index+1) + ": " + todo.taskdescription}</span>
                  </label>
                  {todo.done && <span className="todo-badge">Erledigt</span>}
                </div>
                <div className="todo-actions">
                  <button type="button" onClick={() => handleEditStart(todo.taskdescription)}>Bearbeiten</button>
                  <button type="button" onClick={(event) => handleDelete(event, todo.taskdescription)}>Löschen</button>
                </div>
              </>
            )}
          </li>
        ))}
      </ul>
    );
  }

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="Siegel der Aufgabenliste" />
        <h1 aria-label="Aufgabenliste">
          Aufgaben
          <span>Liste</span>
        </h1>
        <form onSubmit={handleSubmit} className='todo-form'>
          <label htmlFor="taskdescription">Neue Aufgabe eintragen:</label>
          <input
            id="taskdescription"
            type="text"
            value={taskdescription}
            onChange={handleChange}
            placeholder="Aufgabenbeschreibung"
          />
          <button type="submit">Hinzufügen</button>
        </form>
        <div className="todo-controls">
          <label>
            Suche
            <input
              type="text"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Wort oder Kürzel"
            />
          </label>
          <label>
            Status
            <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
              <option value="all">Alle Aufgaben</option>
              <option value="open">Offene Aufgaben</option>
              <option value="done">Erledigte Aufgaben</option>
            </select>
          </label>
          <label>
            Sortieren
            <select value={sortMode} onChange={(event) => setSortMode(event.target.value)}>
              <option value="az">Von A bis Z</option>
              <option value="za">Von Z bis A</option>
            </select>
          </label>
        </div>
        <div>
          {renderTasks(getVisibleTasks())}
        </div>
      </header>
    </div>
  );
}

export default App
