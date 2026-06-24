package com.example.vote.repository;

import com.example.vote.dto.VotingRecordDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * 投票紀錄 Repository 實作 (資料層)
 *
 * 使用 SimpleJdbcCall 呼叫 sp_vote_cast / sp_record_list，
 * 參數皆以繫結方式傳遞，避免 SQL Injection。
 */
@Repository
@RequiredArgsConstructor
public class VotingRecordRepositoryImpl implements VotingRecordRepository {

    private final DataSource dataSource;

    @Override
    public void castVote(String voterName, Long itemId) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_vote_cast");

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
                .withProcedureName("sp_record_list");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_item_id", itemId);

        SqlRowSet rs = call.execute(params).values().stream()
                .filter(v -> v instanceof SqlRowSet)
                .map(v -> (SqlRowSet) v)
                .findFirst()
                .orElse(null);

        List<VotingRecordDTO> result = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                result.add(VotingRecordDTO.builder()
                        .recordId(rs.getLong("record_id"))
                        .voterName(rs.getString("voter_name"))
                        .itemId(rs.getLong("item_id"))
                        .itemName(rs.getString("item_name"))
                        .votedAt(rs.getTimestamp("voted_at") != null
                                ? rs.getTimestamp("voted_at").toLocalDateTime()
                                : null)
                        .build());
            }
        }
        return result;
    }
}
