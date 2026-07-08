import asyncio
from playwright.async_api import async_playwright
async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch(headless=True)
        page = await b.new_page()
        errs = []
        page.on("console", lambda m: errs.append(f"[{m.type}] {m.text}") if m.type in ("error","warning") else None)
        page.on("pageerror", lambda e: errs.append(f"[pageerror] {e}"))
        page.on("requestfailed", lambda r: errs.append(f"[reqfail] {r.method} {r.url}"))
        await page.goto("http://localhost:3000/dealing/ac-deal/detail/DEAL202606230004", wait_until="networkidle", timeout=20000)
        await page.wait_for_timeout(3000)
        await page.screenshot(path="E:/code-project/open-tms/open-tms/logs/ac_detail_after.png", full_page=True)
        print(f"=== {len(errs)} 个错误 ===")
        for e in errs[:20]:
            print(f"  {e[:300]}")
        title = await page.title()
        url = page.url
        body_text = await page.inner_text("body")
        print(f"URL: {url}")
        print(f"TITLE: {title}")
        print(f"BODY len: {len(body_text)}")
        print(f"BODY preview: {body_text[:600]}")
        await b.close()
asyncio.run(main())
