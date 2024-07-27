import os
import sys
import io

template = """<!DOCTYPE html>
<html lang="zh_CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>数据库查询</title>
    <style>
      * {
        padding: 0px;
        margin: 0px;
        box-sizing: border-box;
      }

      body {
        display: flex;
        width: 100vw;
        height: 100vh;
        justify-content: center;
        align-items: center;
        background-color: #b5c9fc;
      }

      .container {
        width: 400px;
        background-color: yellowgreen;
        border-radius: 16px;
        box-shadow: 0px 0px 15px 2px rgba(0, 200, 0, 0.3);
      }

      .num1 .input {
        border: none;
        outline: none;
        border-radius: 15px;
        padding: 1em;
        width: 300px;
        background-color: #ccc;
        box-shadow: inset 2px 5px 10px rgba(0, 0, 0, 0.3);
        transition: 300ms ease-in-out;
      }

      .num1 .input:focus {
        background-color: white;
        transform: scale(1.05);
        box-shadow: 13px 13px 100px #969696, -13px -13px 100px #ffffff;
      }

      form {
        padding: 0px 20px 30px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 20px;
      }

      header {
        font-size: 30px;
        font-weight: bold;
        text-align: center;
        line-height: 3;
        color: darkgreen;
      }

      .cal-btn {
        button {
          align-items: center;
          background-image: linear-gradient(
            144deg,
            #af40ff,
            #5b42f3 50%,
            #00ddeb
          );
          border: 0;
          border-radius: 8px;
          box-shadow: rgba(151, 65, 252, 0.2) 0 15px 30px -5px;
          box-sizing: border-box;
          color: #ffffff;
          display: flex;
          font-family: Phantomsans, sans-serif;
          font-size: 16px;
          justify-content: center;
          line-height: 1em;
          max-width: 100%;
          width: 100px;
          padding: 3px;
          text-decoration: none;
          user-select: none;
          -webkit-user-select: none;
          touch-action: manipulation;
          white-space: nowrap;
          cursor: pointer;
          transition: all 0.3s;
        }

        button:active,
        button:hover {
          outline: 0;
        }

        button span {
          background-color: rgb(5, 6, 45);
          padding: 8px 10px;
          border-radius: 6px;
          width: 100%;
          height: 100%;
          transition: 300ms;
        }

        button:hover span {
          background: none;
        }

        button:active {
          transform: scale(0.9);
        }
      }

      .btn-group {
        width: 80%;
        display: flex;
        justify-content: flex-end;
        align-items: center;
      }

      .res {
        background-color: greenyellow;
        width: 85%;
        /* height: 50px; */
        border-radius: 10px;

        padding: 10px 15px;
        font-weight: bolder;

        span {
          color: #000;
          font-size: 14px;
          display: block;
          text-align: left;
        }

        .line {
          display: flex;
          gap: 30px;
          line-height: 1.8;
          span {
            color: darkgreen;
            font-size: 20px;
          }
        }
      }
    </style>
  </head>
  <body>
    <div class="container">
      <header>数据库查询</header>
      <form action="" method="post">
        <div class="num1">
          <input
            type="text"
            autocomplete="off"
            name="id"
            class="input"
            placeholder="学号"
          />
        </div>
        <div class="btn-group">
          <div class="cal-btn">
            <button type="submit">
              <span class="text">查询</span>
            </button>
          </div>
        </div>
        <div class="res">
          <span>上次查询结果：</span>
            {{}}
        </div>
      </form>
    </div>
  </body>
</html>
"""

import pymysql

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")

if os.environ.get("REQUEST_METHOD") == "GET":
    print("Content-Type: text/html;charset=UTF-8'")
    print()
    content = template.replace("{{}}", "无")
    print(content)
    sys.stdout.flush()

elif os.environ.get("REQUEST_METHOD") == "POST":
    print("Content-Type: text/html;charset=UTF-8")
    print()
    content_length = os.environ.get("Content-Length")
    request_content = sys.stdin.read(int(content_length))
    param_map = {}
    params = request_content.split("&")
    for param in params:
        param_comp = param.split("=")
        param_map[param_comp[0]] = param_comp[1] if len(param_comp) > 1 else ""

    replace_str = """<div class="table">
<div class="line">
    <span>学号</span>
    <span>{}</span>
</div>
<div class="line">
    <span>姓名</span>
    <span>{}</span>
</div>
<div class="line">
    <span>班级</span>
    <span>{}</span>
</div>
</div>
"""
    id = param_map["id"]

    try:
        id = int(id)
        # 连接数据库
        conn = pymysql.connect(
            host="localhost",
            user="wyt",
            password="",
            database="test"
        )
        # 创建游标对象
        cursor = conn.cursor()
        # 编写查询语句
        query = f"SELECT * FROM students WHERE id = {id}"
        # 执行查询语句
        if cursor.execute(query) != 1:
            content = template.replace("{{}}", f"学号 {id} 不存在")
        else:
            # 获取查询结果
            result = cursor.fetchone()
            content = template.replace("{{}}", replace_str.format(result[0], result[1], result[2]))
    except ValueError:
        content = template.replace(
            "{{}}",
            f"学号 {id} 不合法",
        )

    print(content)
    sys.stdout.flush()
