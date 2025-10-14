package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepr.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepr.assignment.individual.entity.Owner;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepr.assignment.individual.persistence.OwnerDao;
import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link OwnerDao} for interacting with the database.
 */
@Repository
public class OwnerJdbcDao implements OwnerDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "owner";
  private static final String SQL_SELECT_BY_ID =
      "SELECT * FROM " + TABLE_NAME
              + " WHERE id = :id";

  private static final String SQL_SELECT_ALL =
      "SELECT * FROM " + TABLE_NAME
              + " WHERE id IN (:ids)";

  private static final String SQL_SELECT_SEARCH =
      "SELECT * FROM " + TABLE_NAME
              + " WHERE UPPER(first_name || ' ' || last_name) LIKE UPPER('%%' || COALESCE(:name, '') || '%%')";

  private static final String SQL_SELECT_SEARCH_LIMIT_CLAUSE = " LIMIT :limit";

  private static final String SQL_CREATE =
      "INSERT INTO " + TABLE_NAME + " (first_name, last_name, email)"
      + " VALUES (:first_name, :last_name, :email)";

  private final JdbcClient jdbcClient;

  @Autowired
  public OwnerJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public Owner getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Owner> owners = jdbcClient
        .sql(SQL_SELECT_BY_ID)
        .param("id", id)
        .query(this::mapRow)
        .list();
    if (owners.isEmpty()) {
      throw new NotFoundException("Owner with ID %d not found".formatted(id));
    }
    if (owners.size() > 1) {
      // If this happens, something is wrong with either the DB or the select
      throw new FatalException("Found more than one owner with ID %d".formatted(id));
    }
    return owners.getFirst();
  }

  @Override
  public Owner create(OwnerCreateDto newOwner) {
    LOG.trace("create({})", newOwner);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

    int created = jdbcClient
        .sql(SQL_CREATE)
        .param("first_name", newOwner.firstName())
        .param("last_name", newOwner.lastName())
        .param("email", newOwner.email())
        .update(keyHolder);

    if (created != 1) {
      throw new FatalException("%d horses inserted, expected exactly 1".formatted(created));
    }

    Number key = keyHolder.getKey();
    if (key == null) {
      // This should never happen. If it does, something is wrong with the DB or the way the prepared statement is set up.
      throw new FatalException("Could not extract key for newly created owner. There is probably a programming error…");
    }

    return new Owner(
        key.longValue(),
        newOwner.firstName(),
        newOwner.lastName(),
        newOwner.email());
  }

  @Override
  public Collection<Owner> getAllById(Collection<Long> ids) {
    LOG.trace("getAllById({})", ids);
    return jdbcClient
        .sql(SQL_SELECT_ALL)
        .param("ids", ids)
        .query(this::mapRow)
        .list();
  }

  @Override
  public Collection<Owner> search(OwnerSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    var query = SQL_SELECT_SEARCH;

    Map<String, Object> params = new HashMap<>();
    params.put("name", searchParameters.name());

    var maxAmount = searchParameters.maxAmount();
    if (maxAmount != null) {
      query += SQL_SELECT_SEARCH_LIMIT_CLAUSE;
      params.put("limit", maxAmount);
    }

    return jdbcClient
        .sql(query)
        .params(params)
        .query(this::mapRow)
        .list();
  }

  private Owner mapRow(ResultSet resultSet, int i) throws SQLException {
    return new Owner(
        resultSet.getLong("id"),
        resultSet.getString("first_name"),
        resultSet.getString("last_name"),
        resultSet.getString("email"));
  }
}
