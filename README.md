# 目的
专门为某位的mc服务器上的全物品分类开发的模组，目的是实现简单的增删改查功能
# pfind/pf
形式为
```
/pfind <language> <query> <limit>
```
language是en, cn, pinyin, pinyin-abbr中的一种, 代表中文, 英文, 拼音全称, 拼音缩写 \
query是查询词语, 支持模糊查询 \
limit是输出的结果数, 可选, 默认为5 \

# pload/pl
形式为
```
/pload
```
该指令只能由管理员调用, 调用时尝试重新加载本地的index.json文件