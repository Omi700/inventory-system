from app.schemas import SummarizeRequest, TextToSqlRequest


def generate_sql(request: TextToSqlRequest) -> tuple[str, str]:
    """Temporary deterministic SQL generator until the real LLM client is wired."""
    question = request.question
    if "低库存" in question or "安全库存" in question:
        return (
            "SELECT p.product_name, w.warehouse_name, i.quantity, p.safe_stock "
            "FROM inventory i "
            "JOIN product p ON i.product_id = p.id "
            "JOIN warehouse w ON i.warehouse_id = w.id "
            "WHERE i.quantity < p.safe_stock "
            "ORDER BY i.quantity ASC LIMIT 20",
            "用户想查询低于安全库存的商品。",
        )
    if "仓库" in question and "库存" in question:
        return (
            "SELECT w.warehouse_name, SUM(i.quantity) AS total_quantity "
            "FROM inventory i "
            "JOIN warehouse w ON i.warehouse_id = w.id "
            "GROUP BY w.id, w.warehouse_name "
            "ORDER BY total_quantity DESC LIMIT 20",
            "用户想查询各仓库库存总量。",
        )
    return (
        "SELECT p.product_name, SUM(s.quantity) AS total_out "
        "FROM stock_out s "
        "JOIN product p ON s.product_id = p.id "
        "WHERE s.created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) "
        "GROUP BY p.id, p.product_name "
        "ORDER BY total_out DESC LIMIT 10",
        "默认按最近 7 天出库排行生成查询。",
    )


def summarize_result(request: SummarizeRequest) -> str:
    if not request.rows:
        return "本次查询没有返回符合条件的数据。"
    first = request.rows[0]
    preview = "，".join(f"{key}: {value}" for key, value in first.items())
    return f"本次查询返回 {len(request.rows)} 条结果，首条结果为：{preview}。"

