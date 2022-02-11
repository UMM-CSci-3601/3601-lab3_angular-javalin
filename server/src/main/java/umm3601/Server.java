package umm3601;

import java.io.IOException;

import io.javalin.Javalin;
import umm3601.user.UserDatabase;
import umm3601.user.UserController;
import umm3601.todo.TodoDatabase;
import umm3601.todo.TodoController;

public class Server {

  private static final int PORT_NUMBER = 4567;
  public static final String USER_DATA_FILE = "/users.json";
  public static final String TODO_DATA_FILE = "/todos.json";

  public static void main(String[] args) {

    // Initialize dependencies
    UserController userController = buildUserController();
    TodoController todoController = buildTodoController();

    Javalin server = Javalin.create().start(PORT_NUMBER);

    // API endpoints

    // Get specific user
    server.get("/api/users/{id}", userController::getUser);

    // List users, filtered using query parameters
    server.get("/api/users", userController::getUsers);

    // Get specific todo
    server.get("/api/todos/{id}", todoController::getTodo);

    // List todos, filtered using query parameters
    server.get("/api/todos", todoController::getTodos);
  }

  /**
   * Create a database using the json file, use it as data source for a new
   * UserController
   *
   * Constructing the controller might throw an IOException if there are problems
   * reading from the JSON "database" file. If that happens we'll print out an
   * error message exit the program.
   */
  private static UserController buildUserController() {
    UserController userController = null;

    try {
      UserDatabase userDatabase = new UserDatabase(USER_DATA_FILE);
      userController = new UserController(userDatabase);
    } catch (IOException e) {
      System.err.println("The server failed to load the user data; shutting down.");
      e.printStackTrace(System.err);

      // Exit from the Java program
      System.exit(1);
    }

    return userController;
  }

  /**
   * Create a database using the json file, use it as data source for a new
   * TodoController
   *
   * Constructing the controller might throw an IOException if there are problems
   * reading from the JSON "database" file. If that happens we'll print out an
   * error message exit the program.
   */
  private static TodoController buildTodoController() {
    TodoController todoController = null;

    try {
      TodoDatabase todoDatabase = new TodoDatabase(TODO_DATA_FILE);
      todoController = new TodoController(todoDatabase);
    } catch (IOException e) {
      System.err.println("The server failed to load the todo data; shutting down.");
      e.printStackTrace(System.err);

      // Exit from the Java program
      System.exit(1);
    }

    return todoController;
  }
}
