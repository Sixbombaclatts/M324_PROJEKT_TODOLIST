import { render, screen, waitFor } from '@testing-library/react';
import App from './App';

describe('App todo list', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('shows no tasks when backend returns an empty list', async () => {
    global.fetch.mockResolvedValueOnce({
      json: async () => [],
    });

    render(<App />);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/');
    });

    expect(screen.queryByText(/Task 1:/)).not.toBeInTheDocument();
  });

  test('shows tasks returned by the backend', async () => {
    global.fetch.mockResolvedValueOnce({
      json: async () => [
        { taskdescription: 'First task', dueDate: '', reminderEnabled: false, done: false },
        { taskdescription: 'Second task', dueDate: '', reminderEnabled: false, done: false },
      ],
    });

    render(<App />);

    expect(await screen.findByText('Task 1: First task')).toBeInTheDocument();
    expect(screen.getByText('Task 2: Second task')).toBeInTheDocument();
  });
});
