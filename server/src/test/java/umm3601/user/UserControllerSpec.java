package umm3601.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.validation.Validation;
import io.javalin.validation.Validator;
import umm3601.Main;

/**
 * Tests the logic of the UserController
 *
 * @throws IOException
 */
// The tests here include a ton of "magic numbers" (numeric constants).
// It wasn't clear to me that giving all of them names would actually
// help things. The fact that it wasn't obvious what to call some
// of them says a lot. Maybe what this ultimately means is that
// these tests can/should be restructured so the constants (there are
// also a lot of "magic strings" that Checkstyle doesn't actually
// flag as a problem) make more sense.
@SuppressWarnings({ "MagicNumber" })
public class UserControllerSpec {

  // A constant so we have a name for the age key in the param maps.
  private static final String AGE_KEY = "age";

  // An instance of the controller we're testing that is prepared in
  // `setUp()`, and then exercised in the various tests below.
  private UserController userController;
  // An instance of our database "layer" that is prepared in
  // `setUp()`, and then used in the tests below.
  private static UserDatabase db;

  // A "fake" version of Javalin's `Context` object that we can
  // use to test with.
  @Mock
  private Context ctx;

  // A captor allows us to make assertions on arguments to method
  // calls that are made "indirectly" by the code we are testing,
  // in this case `json()` calls in `UserController`. We'll use
  // this to make assertions about the data passed to `json()`.
  @Captor
  private ArgumentCaptor<User[]> userArrayCaptor;

  /**
   * Setup the "database" with some example users and
   * create a UserController to exercise in the tests.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @BeforeEach
  public void setUp() throws IOException {
    // Reset our mock context and argument captor
    // (declared above with Mockito annotations @Mock and @Captor)
    MockitoAnnotations.openMocks(this);
    // Construct our "database"
    db = new UserDatabase(Main.USER_DATA_FILE);
    // Construct an instance of our controller which
    // we'll then test.
    userController = new UserController(db);
  }

  /**
   * Verify that we can successfully build a UserController
   * and call it's `addRoutes` method. This doesn't verify
   * much beyond that the code actually runs without throwing
   * an exception. We do, however, confirm that the `addRoutes`
   * causes `.get()` to be called at least twice.
   */
  @Test
  public void canBuildController() throws IOException {
    // Call the `UserController.buildUserController` method
    // to construct a controller instance "by hand".
    UserController controller = UserController.buildUserController(Main.USER_DATA_FILE);
    Javalin mockServer = Mockito.mock(Javalin.class);
    controller.addRoutes(mockServer);

    // Verify that calling `addRoutes()` above caused `get()` to be called
    // on the server at least twice. We use `any()` to say we don't care about
    // the arguments that were passed to `.get()`.
    verify(mockServer, Mockito.atLeast(2)).get(any(), any());
  }

  /**
   * Verify that attempting to build a `UserController` with an
   * invalid `userDataFile` throws an `IOException`.
   */
  @Test
  public void buildControllerFailsWithIllegalDbFile() {
    Assertions.assertThrows(IOException.class, () -> {
      UserController.buildUserController("this is not a legal file name");
    });
  }

  @Test
  public void canGetAllUsers() throws IOException {
    // Call the method on the mock context, which doesn't
    // include any filters, so we should get all the users
    // back.
    userController.getUsers(ctx);

    // Confirm that `json` was called with all the users.
    // The ArgumentCaptor<User[]> userArrayCaptor was initialized in the @BeforeEach
    // Here, we wait to see what happens *when ctx calls the json method* in the
    // call
    // userController.getUsers(ctx) and the json method is passed a User[]
    // (That's when the User[] that was passed as input to the json method is
    // captured)
    verify(ctx).json(userArrayCaptor.capture());
    // Now that the User[] that was passed as input to the json method is captured,
    // we can make assertions about it. In particular, we'll assert that its length
    // is the same as the size of the "database". We could also confirm that the
    // particular users are the same/correct, but that can get complicated
    // since the order of the users in the "database" isn't specified. So we'll
    // just check that the counts are correct.
    assertEquals(db.size(), userArrayCaptor.getValue().length);
  }

  /**
   * Confirm that if we process a request for users with age 25,
   * that all returned users have that age, and we get the correct
   * number of users.
   *
   * The structure of this test is:
   *
   * - We create a `Map` for the request's `queryParams`, that
   * contains a single entry, mapping the `AGE_KEY` to the
   * target value ("25"). This "tells" our `UserController`
   * that we want all the `User`s that have age 25.
   * - We create a validator that confirms that the code
   * we're testing calls `ctx.queryParamsAsClass("age", Integer.class)`,
   * i.e., it asks for the value in the query param map
   * associated with the key `"age"`, interpreted as an Integer.
   * That call needs to return a value of type `Validator<Integer>`
   * that will succeed and return the (integer) value `25` associated
   * with the (`String`) parameter value `"25"`.
   * - We then call `userController.getUsers(ctx)` to run the code
   * being tested with the constructed context `ctx`.
   * - We also use the `userListArrayCaptor` (defined above)
   * to capture the `ArrayList<User>` that the code under test
   * passes to `ctx.json(…)`. We can then confirm that the
   * correct list of users (i.e., all the users with age 25)
   * is passed in to be returned in the context.
   * - Now we can use a variety of assertions to confirm that
   * the code under test did the "right" thing:
   * - Confirm that the list of users has length 2
   * - Confirm that each user in the list has age 25
   * - Confirm that their names are "Jamie" and "Pat"
   *
   * @throws IOException
   */
  @Test
  void canGetUsersWithAge25() throws IOException {
    // We'll need both `String` and `Integer` representations of
    // the target age, so I'm defining both here.
    Integer targetAge = 25;
    String targetAgeString = targetAge.toString();

    // Create a `Map` for the `queryParams` that will "return" the string
    // "37" if you ask for the value associated with the `AGE_KEY`.
    Map<String, List<String>> queryParams = new HashMap<>();

    queryParams.put(AGE_KEY, Arrays.asList(new String[] { targetAgeString }));
    // When the code being tested calls `ctx.queryParamMap()` return the
    // the `queryParams` map we just built.
    when(ctx.queryParamMap()).thenReturn(queryParams);
    // When the code being tested calls `ctx.queryParam(AGE_KEY)` return the
    // `targetAgeString`.
    when(ctx.queryParam(AGE_KEY)).thenReturn(targetAgeString);

    // Create a validator that confirms that when we ask for the value associated
    // with
    // `AGE_KEY` _as an integer_, we get back the integer value 37.
    Validation validation = new Validation();
    // The `AGE_KEY` should be name of the key whose value is being validated.
    // You can actually put whatever you want here, because it's only used in the
    // generation
    // of testing error reports, but using the actually key value will make those
    // reports more informative.
    Validator<Integer> validator = validation.validator(AGE_KEY, Integer.class, targetAgeString);
    // When the code being tested calls `ctx.queryParamAsClass("age",
    // Integer.class)`
    // we'll return the `Validator` we just constructed.
    when(ctx.queryParamAsClass(AGE_KEY, Integer.class))
        .thenReturn(validator);

    userController.getUsers(ctx);

    // Confirm that the code being tested calls `ctx.json(…)`, and capture whatever
    // is passed in as the argument when `ctx.json()` is called.
    verify(ctx).json(userArrayCaptor.capture());
    // Confirm that the code under test calls `ctx.status(HttpStatus.OK)` is called.
    verify(ctx).status(HttpStatus.OK);

    // Confirm that we get back two users.
    assertEquals(2, userArrayCaptor.getValue().length);
    // Confirm that both users have age 37.
    for (User user : userArrayCaptor.getValue()) {
      assertEquals(targetAge, user.age);
    }
    // Generate a list of the names of the returned users.
    List<String> names = Arrays.stream(userArrayCaptor.getValue()).map(user -> user.name).collect(Collectors.toList());
    // Confirm that the returned `names` contain the two names of the
    // 25-year-olds.
    System.err.println(names);
    assertTrue(names.contains("Connie Stewart"));
    assertTrue(names.contains("Lynn Ferguson"));
  }

  /**
   * Test that if the user sends a request with an illegal value in
   * the age field (i.e., something that can't be parsed to a number)
   * we get a reasonable error code back.
   */
  @Test
  public void respondsAppropriatelyToIllegalAge() {
    // We'll set the requested "age" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("age", Arrays.asList(new String[] { "abc" }));
    // Tell the mock `ctx` object to return our query
    // param map when `queryParamMap()` is called.
    when(ctx.queryParamMap()).thenReturn(queryParams);

    // This should now throw a `BadRequestResponse` exception because
    // our request has an age that can't be parsed to a number.
    Throwable exception = Assertions.assertThrows(BadRequestResponse.class, () -> {
      userController.getUsers(ctx);
    });
    assertEquals("Specified age '" + "abc" + "' can't be parsed to an integer", exception.getMessage());
  }

  /**
   * Confirm that we can get all the users with company OHMNET.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetUsersWithCompany() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("company", Arrays.asList(new String[] { "OHMNET" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    userController.getUsers(ctx);

    // Confirm that all the users passed to `json` work for OHMNET.
    verify(ctx).json(userArrayCaptor.capture());
    for (User user : userArrayCaptor.getValue()) {
      assertEquals("OHMNET", user.company);
    }
  }

  @Test
  public void getUsersByRole() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("role", Arrays.asList(new String[] { "viewer" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    when(ctx.status()).thenReturn(HttpStatus.OK);
    userController.getUsers(ctx);

    verify(ctx).json(userArrayCaptor.capture());
    assertEquals(5, userArrayCaptor.getValue().length);
  }

  /**
   * Confirm that we can get all the users with age 25 and company OHMNET.
   * This is a "combination" test that tests the interaction of the
   * `age` and `company` query parameters.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetUsersWithGivenAgeAndCompany() throws IOException {

    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("company", Arrays.asList(new String[] { "OHMNET" }));
    queryParams.put("age", Arrays.asList(new String[] { "25" }));
    when(ctx.queryParamMap()).thenReturn(queryParams);

    userController.getUsers(ctx);

    // Confirm that all the users passed to `json` work for OHMNET
    // and have age 25.
    verify(ctx).json(userArrayCaptor.capture());
    for (User user : userArrayCaptor.getValue()) {
      assertEquals(25, user.age);
      assertEquals("OHMNET", user.company);
    }
    assertEquals(1, userArrayCaptor.getValue().length);
  }

  /**
   * Confirm that we get a user when using a valid user ID.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void canGetUserWithSpecifiedId() throws IOException {
    String id = "588935f5c668650dc77df581";
    User user = db.getUser(id);

    when(ctx.pathParam("id")).thenReturn(id);

    userController.getUser(ctx);

    verify(ctx).json(user);
    verify(ctx).status(HttpStatus.OK);
  }

  /**
   * Confirm that we get a 404 Not Found response when
   * we request a user ID that doesn't exist.
   *
   * @throws IOException if there are problems reading from the "database" file.
   */
  @Test
  public void respondsAppropriatelyToRequestForNonexistentId() throws IOException {
    when(ctx.pathParam("id")).thenReturn(null);
    Throwable exception = Assertions.assertThrows(NotFoundResponse.class, () -> {
      userController.getUser(ctx);
    });
    assertEquals("No user with id " + null + " was found.", exception.getMessage());
  }
}
