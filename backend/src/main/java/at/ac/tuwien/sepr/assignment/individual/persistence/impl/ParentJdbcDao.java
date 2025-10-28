package at.ac.tuwien.sepr.assignment.individual.persistence.impl;

import at.ac.tuwien.sepr.assignment.individual.entity.Parent;
import at.ac.tuwien.sepr.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepr.assignment.individual.persistence.ParentDao;
import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link ParentDao} for interacting with the database.
 */
@Repository
public class ParentJdbcDao implements ParentDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse_parent";

  private static final String SQL_SELECT_BY_HORSE_ID =
      "SELECT horse_id, parent_id FROM " + TABLE_NAME
          + " WHERE horse_id = :horse_id ORDER BY parent_id";

  private static final String SQL_SELECT_BY_PARENT_ID =
      "SELECT horse_id, parent_id FROM " + TABLE_NAME
          + " WHERE parent_id = :parent_id ORDER BY horse_id";

  private static final String SQL_DELETE_BY_HORSE_ID =
      "DELETE FROM " + TABLE_NAME + " WHERE horse_id = :horse_id";

  private static final String SQL_DELETE_BY_PARENT_ID =
      "DELETE FROM " + TABLE_NAME + " WHERE parent_id = :parent_id";

  private static final String SQL_INSERT =
      "INSERT INTO " + TABLE_NAME + " (horse_id, parent_id) VALUES (:horse_id, :parent_id)";

  private final JdbcClient jdbcClient;

  @Autowired
  public ParentJdbcDao(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public List<Parent> getParentsByHorseId(long horseId) {
    LOG.trace("getParentsByHorseId({})", horseId);
    return jdbcClient
        .sql(SQL_SELECT_BY_HORSE_ID)
        .param("horse_id", horseId)
        .query(this::mapRow)
        .list();
  }

  @Override
  public List<Parent> getChildrenByParentId(long parentId) {
    LOG.trace("getChildrenByParentId({})", parentId);
    return jdbcClient
        .sql(SQL_SELECT_BY_PARENT_ID)
        .param("parent_id", parentId)
        .query(this::mapRow)
        .list();
  }

  @Override
  public void setParents(long horseId, List<Long> parentIds) {
    LOG.trace("setParents({}, {})", horseId, parentIds);

    // First delete existing relationships
    deleteParentsByHorseId(horseId);

    // Then insert new relationships
    for (Long parentId : parentIds) {
      int inserted = jdbcClient
          .sql(SQL_INSERT)
          .param("horse_id", horseId)
          .param("parent_id", parentId)
          .update();

      if (inserted != 1) {
        throw new FatalException("%d parent relationships inserted, expected exactly 1".formatted(inserted));
      }
    }
  }

  @Override
  public void deleteParentsByHorseId(long horseId) {
    LOG.trace("deleteParentsByHorseId({})", horseId);
    jdbcClient
        .sql(SQL_DELETE_BY_HORSE_ID)
        .param("horse_id", horseId)
        .update();
  }

  @Override
  public void deleteChildrenByParentId(long parentId) {
    LOG.trace("deleteChildrenByParentId({})", parentId);
    jdbcClient
        .sql(SQL_DELETE_BY_PARENT_ID)
        .param("parent_id", parentId)
        .update();
  }

  private Parent mapRow(ResultSet result, int rownum) throws SQLException {
    return new Parent(
        result.getLong("horse_id"),
        result.getLong("parent_id"));
  }
}
