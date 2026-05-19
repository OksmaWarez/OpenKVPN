package dev.brighten.antivpn.database.local.version;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.database.DatabaseException;
import dev.brighten.antivpn.database.VPNDatabase;
import dev.brighten.antivpn.database.sql.utils.Query;
import dev.brighten.antivpn.database.version.Version;
import java.sql.SQLException;

public class Fourth implements Version<VPNDatabase> {

  @Override
  public void update(VPNDatabase database) throws DatabaseException {
    try (var statement =
        Query.prepare("ALTER TABLE `responses` ADD COLUMN `hosting` boolean DEFAULT false")) {
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to add hosting column to responses table", e);
    }

    try (var statement =
        Query.prepare("INSERT INTO `database_version` (`version`) VALUES (?)")
            .append(versionNumber())) {
      statement.execute();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to update database version to 3", e);
    }

    AntiVPN.getInstance().getExecutor().log("Added hosting column to responses table.");
  }

  @Override
  public int versionNumber() {
    return 3;
  }

  @Override
  public boolean needsUpdate(VPNDatabase database) {
    try (var statement = Query.prepare("select * from `database_version` where version = 3")) {
      try (var set = statement.executeQuery()) {
        return !set.next();
      }
    } catch (SQLException e) {
      return true;
    }
  }
}
