package com.example.vote.repository;

import com.example.vote.dto.VotingRecordDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * 投票紀錄 Repository 實作 (資料層)
 *
 * 使用 SimpleJdbcCall 呼叫 sp_vote_cast / sp_record_list，
 * 參數皆以繫結方式傳遞，避免 SQL Injection。
 *
 * 明確宣告參數與回傳的 ResultSet (withoutProcedureColumnMetaDataAccess +
 * declareParameters/returningResultSet)，避免 SimpleJdbcCall 自動探測
 * MySQL Stored Procedure metadata 時在某些環境下解析失敗。
 */
@Repository
@RequiredArgsConstructor
public class VotingRecordRepositoryImpl implements VotingRecordRepository {

    private final DataSource dataSource;

    @Override
    public void castVote(String voterName, Long itemId) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_vote_cast")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_voter_name", Types.VARCHAR),
                        new SqlParameter("p_item_id", Types.BIGINT)
                );

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_voter_name", voterName)
                .addValue("p_item_id", itemId);

        // 若項目不存在/已刪除，sp_vote_cast 內部會 SIGNAL SQLSTATE '45000'
        // Spring JDBC 會將其轉換為 DataAccessException，由上層 Service / GlobalExceptionHandler 處理
        call.execute(params);
    }

    @Override
    public List<VotingRecordDTO> findAll(Long itemId) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_record_list")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlParameter("p_item_id", Types.BIGINT))
                .returningResultSet("#result-set-1", (rs, rowNum) -> VotingRecordDTO.builder()
                        .recordId(rs.getLong("record_id"))
                        .voterName(rs.getString("voter_name"))
                        .itemId(rs.getLong("item_id"))
                        .itemName(rs.getString("item_name"))
                        .votedAt(rs.getTimestamp("voted_at") != null
                                ? rs.getTimestamp("voted_at").toLocalDateTime()
                                : null)
                        .build());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_item_id", itemId);

        @SuppressWarnings("unchecked")
        List<VotingRecordDTO> result = (List<VotingRecordDTO>) call.execute(params).get("#result-set-1");

        return result != null ? result : new ArrayList<>();
    }
}
