package ibf2022.batch2.miniProject.server.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import ibf2022.batch2.miniProject.server.model.ERole;
import ibf2022.batch2.miniProject.server.model.Role;
import ibf2022.batch2.miniProject.server.model.User;

@Repository
public class UserRepository {

  private final static String FIND_USERNAME = "select * from users where username=?";
  private final static String FIND_EMAIL = "select * from users where email=?";
  private final static String FIND_ROLE = "select id from roles where name=?";
  private final static String INSERT_USER = "insert into users (username, email, password) values (?,?,?)";
  private final static String GET_USER_ID = "select id from users where username=? and email=?";
  private final static String INSERT_USER_ROLES = "insert into user_roles (user_id, role_id) values (?,?)";
  private final static String GET_ROLE_ID = "select role_id from user_roles where user_id=?";
  private final static String GET_ROLES = "select name from roles where id=?";

  @Autowired
  @Qualifier("jdbcTemplate2")
  private JdbcTemplate jdbcTemplate2;

  public Optional<User> findByUsername(String username) {
    User user = jdbcTemplate2.query(FIND_USERNAME, new ResultSetExtractor<User>() {
      @Override
      public User extractData(ResultSet rs) throws SQLException {
        if (rs.next()) {
          User user = new User();
          user.setId(Long.parseLong(Integer.toString(rs.getInt("id"))));
          user.setUsername(rs.getString("username"));
          user.setEmail(rs.getString("email"));
          user.setPassword(rs.getString("password"));
          return user;
        } else {
          return null;
        }
      }
    }, username);

    if (user == null) {
      return Optional.empty();
    } else {
      List<Integer> roleIds = jdbcTemplate2.query(GET_ROLE_ID, new ResultSetExtractor<List<Integer>>() {
        @Override
        public List<Integer> extractData(ResultSet rs) throws SQLException {
          List<Integer> listOfRoleId = new LinkedList<>();
          while (rs.next()) {
            listOfRoleId.add(rs.getInt("role_id"));
          }
            return listOfRoleId;         
        }
      }, user.getId());

      List<String> listOfRoleNames = new LinkedList<>();
      for (Integer r : roleIds) {
        String role = jdbcTemplate2.query(GET_ROLES, new ResultSetExtractor<String>() {
          @Override
          public String extractData(ResultSet rs) throws SQLException {
            rs.next();
            return rs.getString("name");
          }
        }, r);

        listOfRoleNames.add(role);
      }

      Set<Role> roles = new HashSet<>();
      
      for (String r : listOfRoleNames) {
        ERole e = ERole.valueOf(r);
        Role ro = new Role(e);
        roles.add(ro);
      }

      user.setRoles(roles);
      return Optional.of(user);
      
    }
}

  public Boolean existsByUsername(String username) {

    Boolean exists = jdbcTemplate2.query(FIND_USERNAME, new ResultSetExtractor<Boolean>() {
      @Override
      public Boolean extractData(ResultSet rs) throws SQLException {
        if (rs.next()) {
          return true;
        } else {
          return false;
        }
      }
    }, username);

    return exists;
  };

  public Boolean existsByEmail(String email) {
    Boolean exists = jdbcTemplate2.query(FIND_EMAIL, new ResultSetExtractor<Boolean>() {
      @Override
      public Boolean extractData(ResultSet rs) throws SQLException {
        if (rs.next()) {
          return true;
        } else {
          return false;
        }
      }
    }, email);

    return exists;
  }

  public Boolean save(User user) {
    List<Integer> listOfRoles = new LinkedList<>();
    
    for (Role role : user.getRoles()) {
      String roleStr = role.getName().name();

      listOfRoles.add(jdbcTemplate2.query(FIND_ROLE, new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException {
          if (rs.next()) {
            return rs.getInt("id");
          } else {
            return null;
          }
        }
      }, roleStr));

    }

    Integer updatedUser = jdbcTemplate2.update(INSERT_USER, user.getUsername(), user.getEmail(), user.getPassword());

    if (updatedUser == 0) {
      return false;
    } else {
      Integer userId = jdbcTemplate2.query(GET_USER_ID, new ResultSetExtractor<Integer>() {
        @Override
        public Integer extractData(ResultSet rs) throws SQLException {
          if (rs.next()) {
            return rs.getInt("id");
          } else {
            return null;
          }
        }
      }, user.getUsername(), user.getEmail());

      for (Integer r : listOfRoles) {
        Integer updatedUserRole = jdbcTemplate2.update(INSERT_USER_ROLES, userId, r);

        if (updatedUserRole == 0) {
          return false;
        }
      }

      return true;
      
    }
  }
}





