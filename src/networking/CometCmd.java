package networking;
/**
 * Possible commands to pass from server to client
 * @author stevearc
 *
 */

public enum CometCmd {
DELETE_TABLE_ROW,
INSERT_TABLE_ROW,
START_RUN,
FINISH_RUN,
MATCH_FINISHED,
RUN_ERROR,
ADD_MAP,
REMOVE_MAP,
RELOAD,
}
