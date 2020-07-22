# mybatis-plus-batch-spring-boot-starter
mybatis-plus 批量插入/更新功能增强

# 使用方法
1. pom.xml 引入依赖:
```xml
 <dependency>
   <groupId>com.beyond</groupId>
   <artifactId>mybatis-plus-batch-spring-boot-starter</artifactId>
   <version>1.0</version>
</dependency>
```
2. Mapper 继承 CustomBaseMapper
```java
@Mapper
@Repository
public interface BookMapper extends CustomBaseMapper<Book> {}
```
3. 开始使用
- 插入/更新调用过set方法的字段, 即使是设置成null也会更新
```java
List<User> userListForUpdate = new ArrayList<>();
for (int i = 0; i < size; i++) {
    User user = BatchHelper.getBatchEntity(User.class);
    user.setName("hello"+i);
    user.setAge(i+10);
    userListForUpdate.add(user);
}
userMapper.insertSetColumnsBatch(userListForUpdate);
userMapper.updateSetColumnsBatchById(userListForUpdate);
```
- 插入/更新不为null的字段, 如列表中对象某对象不全为null, 也会更新字段为null
```java
List<User> userListForUpdate = new ArrayList<>();
for (int i = 0; i < size; i++) {
    User user = new User();
    user.setName("hello"+i);
    user.setAge(i+10);
    userListForUpdate.add(user);
}
userMapper.insertNotNullColumnsBatch(userListForUpdate);
userMapper.updateNotNullColumnsBatchById(userListForUpdate);
```
- 自动决定插入/更新方式: 如果对象实现了BatchWrapper接口, 则用 insertSetColumnsBatch/updateSetColumnsBatchById, 否则使用 insertNotNullColumnsBatch/updateNotNullColumnsBatchById
```java
List<User> userListForUpdate = new ArrayList<>();
for (int i = 0; i < size; i++) {
//   1. User user = BatchHelper.getBatchEntity(User.class);
//   2. User user = new User();
    user.setName("hello"+i);
    user.setAge(i+10);
    userListForUpdate.add(user);
}
userMapper.insertBatch(userListForUpdate);
userMapper.updateBatchById(userListForUpdate);
```
PS: BatchHelper.getBatchEntity(User.class) 方法是采用cglib生成User.class的子类, 性能比其他方法慢 5-10倍. 且首次使用会消耗更长时间
解决: 
1. 首次执行慢的问题: BatchHelper.warmCache("entity的package路径(如com.beyond.entity)")
2. 如需批量执行大量数据, 可自己实现 BatchWrapper ,继承entity类
```java
/** auto generate by BatchWrapperGenerator in test package*/
public class UserBatchWrapper extends User implements BatchWrapper {

    private Set<String> setColNames = new HashSet<>();
    private Map<String, Object> colName2ValueMap = new HashMap<>();

    private String idColName;

    public UserBatchWrapper() {
        idColName = "id";
    }

    @Override
    public void setId(Integer id) {
        super.setId(id);
        colName2ValueMap.put("id", id);
        setColNames.add("id");
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        colName2ValueMap.put("name", name);
        setColNames.add("name");
    }

    @Override
    public void setAge(Integer age) {
        super.setAge(age);
        colName2ValueMap.put("age", age);
        setColNames.add("age");
    }

    @Override
    public Set<String> getSetColNames() {
        return setColNames;
    }

    @Override
    public void setSetColNames(Set<String> setColNames) {
        this.setColNames = setColNames;
    }

    @Override
    public Map<String, Object> getColName2ValueMap() {
        return colName2ValueMap;
    }

    @Override
    public void setColName2ValueMap(Map<String, Object> colName2ValueMap) {
        this.colName2ValueMap = colName2ValueMap;
    }

    @Override
    public String getIdColName() {
        return idColName;
    }

    @Override
    public void setIdColName(String idColName) {
        this.idColName = idColName;
    }
}
```
3. 性能对比: 
- 自己书写继承entity类的方法与 NotNull 的实现方式, 速度接近, 10000条数据 0-2ms,
- BatchHelper.getBatchEntity(User.class) 的动态生成代理类的方法, 因为用到了cglib生成字节码技术, 速度较慢, 10000条数据 6-10ms(执行 warmCache 方法后)

### 发布到maven私服
pom.xml 添加
```xml
    <distributionManagement>
        <repository>
            <id>nexus</id>
            <name>Nexus Repository</name>
            <url>http://xxx.xxx.com:8081/repository/maven-releases/</url>
        </repository>
    </distributionManagement>
```