import os
import sys
import io

template = """<!DOCTYPE html>
<html lang="zh_CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>计算器</title>
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

      :focus {
        outline: 0;
        border-color: #2260ff;
        box-shadow: 0 0 0 4px #b5c9fc;
      }

      .option {
        display: flex;
        justify-content: center;
        align-items: center;
      }

      .option input[type="radio"] {
        clip: rect(0 0 0 0);
        clip-path: inset(100%);
        height: 1px;
        overflow: hidden;
        position: absolute;
        white-space: nowrap;
        width: 1px;
      }

      .option input[type="radio"]:checked + span {
        box-shadow: 0 0 0 0.0625em #0043ed;
        background-color: #dee7ff;
        z-index: 1;
        color: #0043ed;
      }

      label span {
        display: block;
        cursor: pointer;
        background-color: #fff;
        padding: 0.375em 0.75em;
        position: relative;
        margin-left: 0.0625em;
        box-shadow: 0 0 0 0.0625em #b5bfd9;
        letter-spacing: 0.05em;
        color: #3e4963;
        text-align: center;
        transition: background-color 0.5s ease;
      }

      label:first-child span {
        border-radius: 0.375em 0 0 0.375em;
      }

      label:last-child span {
        border-radius: 0 0.375em 0.375em 0;
      }

      .num1 .input,
      .num2 .input {
        border: none;
        outline: none;
        border-radius: 15px;
        padding: 1em;
        width: 300px;
        background-color: #ccc;
        box-shadow: inset 2px 5px 10px rgba(0, 0, 0, 0.3);
        transition: 300ms ease-in-out;
      }

      .num1 .input:focus,
      .num2 .input:focus {
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
        width: 90%;
        display: flex;
        align-content: center;
        align-items: center;
        vertical-align: middle;
        justify-content: space-around;
      }

      .res {
        background-color:  greenyellow;
        width: 85%;
        /* height: 50px; */
        border-radius: 10px;
        color: darkgreen;
        font-size: 20px;
        font-weight: bolder;
        text-align: center;
        padding: 10px 15px;

        span {
            color: #000;
            font-size: 14px;
            display: block;
            text-align: left;
        }
      }
    </style>
  </head>
  <body>
    <div class="container">
      <header>在线计算器</header>
      <form action="" method="post">
        <div class="num1">
          <input
            type="text"
            autocomplete="off"
            name="num1"
            class="input"
            placeholder="第一个数"
          />
        </div>
        <div class="num2">
          <input
            type="text"
            autocomplete="off"
            name="num2"
            class="input"
            placeholder="第二个数"
          />
        </div>
        <div class="btn-group">
          <div class="option">
            <label>
              <input type="radio" name="op" value="add" checked="" />
              <span>加</span>
            </label>
            <label>
              <input type="radio" name="op" value="sub"/>
              <span>减</span>
            </label>
            <label>
              <input type="radio" name="op" value="mul"/>
              <span>乘</span>
            </label>
            <label>
              <input type="radio" name="op" value="div"/>
              <span>除</span>
            </label>
          </div>
          <div class="cal-btn">
            <button type="submit">
              <span class="text">计算结果</span>
            </button>
          </div>
        </div>
        <div class="res">
            <span>上次计算结果：</span>
            {{}}
        </div>
      </form>
    </div>
  </body>
</html>
"""

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")

if os.environ.get("REQUEST_METHOD") == "GET":
    print("Content-Type: text/html;charset=utf-8'")
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

    replace_str = "{} {} {} = {}"
    op = param_map["op"]
    num1 = param_map["num1"]
    num2 = param_map["num2"]
    res = None

    try:
        num1 = float(num1)
        num2 = float(num2)
        if op == "add":
            res = num1 + num2
            op = "+"
        elif op == "sub":
            res = num1 - num2
            op = "-"
        elif op == "mul":
            res = num1 * num2
            op = "*"
        elif op == "div":
            res = num1 / num2
            op = "/"
        content = template.replace("{{}}", replace_str.format(num1, op, num2, res))
    except ValueError:
        content = template.replace(
            "{{}}",
            replace_str.format(num1, op, num2, "ERROR"),
        )

    print(content)
    sys.stdout.flush()
