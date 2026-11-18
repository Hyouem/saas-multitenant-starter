package com.hyouem.portfolio;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") public class EntryController {
 private final JdbcTemplate jdbc;
 public EntryController(JdbcTemplate jdbc) { this.jdbc=jdbc; }
 private String tenant(String value) { if(value==null || !value.matches("[a-zA-Z0-9_-]{2,40}")) throw new IllegalArgumentException("Tenant inválido"); return value; }
 @GetMapping("/health") public Map<String,String> health() { return Map.of("status","UP","project","saas-multitenant-starter"); }
 @GetMapping("/entries") public List<Map<String,Object>> list(@RequestHeader(value="X-Tenant",defaultValue="demo") String tenant) {
  return jdbc.query("select id,field1, field2, field3,created_at from entries where tenant=? order by id desc",(rs,i)->{
   Map<String,Object> m=new LinkedHashMap<>();m.put("id",rs.getLong("id"));
   m.put("title",rs.getString("field1"));m.put("status",rs.getString("field2"));m.put("owner",rs.getString("field3"));
   m.put("createdAt",rs.getTimestamp("created_at").toInstant().toString());return m;
  },tenant(tenant));
 }
 @PostMapping("/entries") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> create(@RequestHeader(value="X-Tenant",defaultValue="demo") String tenant,@RequestBody Map<String,String> values) {
  tenant=tenant(tenant);

  for(String key:List.of("title","status","owner")) if(values.getOrDefault(key,"").isBlank()) throw new IllegalArgumentException("Campo obrigatório: "+key);
  Long id=jdbc.queryForObject("insert into entries (tenant,field1, field2, field3) values (?,?,?,?) returning id",Long.class,tenant,values.get("title"), values.get("status"), values.get("owner"));
  return Map.of("id",id,"status","CREATED");
 }
 @DeleteMapping("/entries/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@RequestHeader(value="X-Tenant",defaultValue="demo") String tenant,@PathVariable long id) {
  if(jdbc.update("delete from entries where id=? and tenant=?",id,tenant(tenant))==0) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
 }
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e) {return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
}
