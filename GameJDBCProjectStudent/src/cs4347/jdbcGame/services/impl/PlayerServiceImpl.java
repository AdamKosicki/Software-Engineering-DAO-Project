/* NOTICE: All materials provided by this project, and materials derived 
 * from the project, are the property of the University of Texas. 
 * Project materials, or those derived from the materials, cannot be placed 
 * into publicly accessible locations on the web. Project materials cannot 
 * be shared with other project teams. Making project materials publicly 
 * accessible, or sharing with other project teams will result in the 
 * failure of the team responsible and any team that uses the shared materials. 
 * Sharing project materials or using shared materials will also result 
 * in the reporting of all team members for academic dishonesty. 
 */
package cs4347.jdbcGame.services.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import cs4347.jdbcGame.dao.CreditCardDAO;
import cs4347.jdbcGame.dao.PlayerDAO;
import cs4347.jdbcGame.dao.impl.CreditCardDAOImpl;
import cs4347.jdbcGame.dao.impl.PlayerDAOImpl;
import cs4347.jdbcGame.entity.CreditCard;
import cs4347.jdbcGame.entity.Player;
import cs4347.jdbcGame.services.PlayerService;
import cs4347.jdbcGame.util.DAOException;

public class PlayerServiceImpl implements PlayerService
{
    private DataSource dataSource;

    public PlayerServiceImpl(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public Player create(Player player) throws DAOException, SQLException
    {
        if (player.getCreditCards() == null || player.getCreditCards().size() == 0) {
            throw new DAOException("Player must have at lease one CreditCard");
        }

        PlayerDAO playerDAO = new PlayerDAOImpl();
        CreditCardDAO ccDAO = new CreditCardDAOImpl();

        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            Player p1 = playerDAO.create(connection, player);
            Long playerID = p1.getId();
            for (CreditCard creditCard : player.getCreditCards()) {
                creditCard.setPlayerID(playerID);
                ccDAO.create(connection, creditCard, playerID);
            }
            connection.commit();
            return p1;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Override
    public Player retrieve(Long playerID) throws DAOException, SQLException
    {
        PlayerDAO playerDAO = new PlayerDAOImpl();
        CreditCardDAO ccDAO = new CreditCardDAOImpl();

        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            Player p1 = playerDAO.retrieve(connection, playerID);
            if (p1 == null) {
                return null;
            }

            List<CreditCard> ccList = ccDAO.retrieveCreditCardsForPlayer(connection, playerID);
            p1.setCreditCards(ccList);
            connection.commit();
            return p1;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Override
    public int update(Player player) throws DAOException, SQLException
    {
        PlayerDAO playerDAO = new PlayerDAOImpl();
        CreditCardDAO ccDAO = new CreditCardDAOImpl();

        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            // Instead of merging existing and new CC, the plan is to
            // delete all CC assoc with player ID and create the CCs
            // belonging to the given player.
            ccDAO.deleteForPlayer(connection, player.getId());
            for (CreditCard creditCard : player.getCreditCards()) {
                // in case reinserting a previously persisted CC
                creditCard.setId(null);
                ccDAO.create(connection, creditCard, player.getId());
            }

            int count = playerDAO.update(connection, player);
            connection.commit();
            return count;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }

    }

    @Override
    public int delete(Long playerID) throws DAOException, SQLException
    {
        PlayerDAO playerDAO = new PlayerDAOImpl();
        CreditCardDAO ccDAO = new CreditCardDAOImpl();

        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            @SuppressWarnings("unused")
            int ccRemoved = ccDAO.deleteForPlayer(connection, playerID);
            int count = playerDAO.delete(connection, playerID);
            connection.commit();
            return count;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Override
    public int count() throws DAOException, SQLException
    {
        PlayerDAO playerDAO = new PlayerDAOImpl();

        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            @SuppressWarnings("unused")
            int count = playerDAO.count(connection);
            connection.commit();
            return count;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Override
    public List<Player> retrieveByJoinDate(Date start, Date end) throws DAOException, SQLException
    {
        PlayerDAO playerDAO = new PlayerDAOImpl();

        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            @SuppressWarnings("unused")
            List<Player> playerList = playerDAO.retrieveByJoinDate(connection, start, end);
            connection.commit();
            return playerList;
        } catch (Exception ex) {
            connection.rollback();
            throw ex;
        }
        finally {
            if (connection != null) {
                connection.setAutoCommit(true);
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    /**
     * Used for debugging and testing purposes.
     */
    @Override
    public int countCreditCardsForPlayer(Long playerID) throws DAOException, SQLException
    {
        CreditCardDAO ccDAO = new CreditCardDAOImpl();
        Connection connection = dataSource.getConnection();

        List<CreditCard> ccList = ccDAO.retrieveCreditCardsForPlayer(connection, playerID);
        return ccList.size();
    }

}
