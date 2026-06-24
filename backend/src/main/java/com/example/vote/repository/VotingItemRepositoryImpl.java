package com.example.vote.repository;

import com.example.vote.dto.VotingItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 投票項目 Repository 實作 (資料層)
 *
 * 使用 Spring JDBC 的 SimpleJdbcCall 呼叫 Stored Procedure。
 * 所有參數皆以 IN/OUT 參數繫結傳遞，不進行任何字串拼接 SQL，
 * 從根本上防止 SQL Injection。
 */
@Repository
@RequiredArgsConstructor
public class VotingItemRepositoryImpl implements VotingItemRepository {

    private final DataSource dataSource;

    @Override
    public List<VotingItemDTO> findAll() {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_item_list");

        SqlRowSet rs = call.execute().values().stream()
                .filter(v -> v instanceof SqlRowSet)
                .map(v -> (SqlRowSet) v)
                .findFirst()
                .orElse(null);

        List<VotingItemDTO> result = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public Optional<VotingItemDTO> findById(Long itemId) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_item_get");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_item_id", itemId);

        SqlRowSet rs = call.execute(params).values().stream()
                .filter(v -> v instanceof SqlRowSet)
                .map(v -> (SqlRowSet) v)
                .findFirst()
                .orElse(null);

        if (rs != null && rs.next()) {
            return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    @Override
    public Long create(String itemName) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_item_create");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_item_name", itemName);

        var out = call.execute(params);
        Object newId = out.get("p_new_id");

        if (newId instanceof BigInteger bi) {
            return bi.longValue();
        } else if (newId instanceof Number n) {
            return n.longValue();
        }
        throw new EmptyResultDataAccessException("新增投票項目失敗，未取得新編號", 1);
    }

    @Override
    public int update(Long itemId, String itemName) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_item_update");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_item_id", itemId)
                .addValue("p_item_name", itemName);

        call.execute(params);
        // sp_item_update 為 UPDATE 語句，透過再次查詢確認是否真的命中資料列
        return findById(itemId).filter(i -> i.getItemName().equals(itemName)).isPresent() ? 1 : 0;
    }

    @Override
    public int softDelete(Long itemId) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_item_delete");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_item_id", itemId);

        call.execute(params);
        // 刪除後應查不到該筆資料(軟刪除)，回傳 1 代表已生效
        return findById(itemId).isEmpty() ? 1 : 0;
    }

    private VotingItemDTO mapRow(SqlRowSet rs) {
        return VotingItemDTO.builder()
                .itemId(rs.getLong("item_id"))
                .itemName(rs.getString("item_name"))
                .voteCount(rs.getInt("vote_count"))
                .createdAt(toLocalDateTime(rs.getTimestamp("created_at")))
                .updatedAt(toLocalDateTime(rs.getTimestamp("updated_at")))
                .build();
    }

    private LocalDateTime toLocalDateTime(java.sql.Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
