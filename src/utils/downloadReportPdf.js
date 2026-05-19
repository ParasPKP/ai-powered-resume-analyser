const escapeHtml = (value) => String(value)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/\"/g, '&quot;')
  .replace(/'/g, '&#39;');

const toLabel = (key) => key
  .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
  .replace(/_/g, ' ')
  .replace(/\b\w/g, (char) => char.toUpperCase());

const renderValue = (value) => {
  if (value === null || value === undefined) {
    return '<span class="muted">Not available</span>';
  }

  if (Array.isArray(value)) {
    if (value.length === 0) {
      return '<span class="muted">None</span>';
    }

    return `<ul>${value.map((item) => `<li>${renderValue(item)}</li>`).join('')}</ul>`;
  }

  if (typeof value === 'object') {
    const entries = Object.entries(value);
    if (entries.length === 0) {
      return '<span class="muted">None</span>';
    }

    return `
      <table>
        <tbody>
          ${entries.map(([key, nestedValue]) => `
            <tr>
              <th>${escapeHtml(toLabel(key))}</th>
              <td>${renderValue(nestedValue)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  }

  return `<span>${escapeHtml(value)}</span>`;
};

export function downloadReportPdf({ title, filePrefix, data }) {
  const reportWindow = window.open('', '_blank', 'width=1000,height=900');

  if (!reportWindow) {
    alert('Popup was blocked. Please allow popups and try again to save the PDF report.');
    return;
  }

  const timestamp = new Date();
  const fileDate = timestamp.toISOString().slice(0, 10);
  const readableDate = timestamp.toLocaleString();

  reportWindow.document.write(`
    <!doctype html>
    <html lang="en">
      <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>${escapeHtml(filePrefix)}-${fileDate}</title>
        <style>
          :root {
            color-scheme: light;
          }
          * {
            box-sizing: border-box;
          }
          body {
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 24px;
            color: #111827;
            background: #f3f4f6;
            line-height: 1.5;
          }
          .report {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            padding: 24px;
            max-width: 920px;
            margin: 0 auto;
          }
          h1 {
            margin: 0 0 8px;
            font-size: 28px;
          }
          .meta {
            color: #4b5563;
            margin-bottom: 18px;
          }
          .hint {
            font-size: 14px;
            color: #6b7280;
            margin-bottom: 20px;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            margin: 8px 0 14px;
            table-layout: fixed;
          }
          th,
          td {
            border: 1px solid #e5e7eb;
            padding: 10px;
            vertical-align: top;
            word-wrap: break-word;
            overflow-wrap: anywhere;
          }
          th {
            width: 32%;
            text-align: left;
            background: #f9fafb;
            color: #111827;
          }
          ul {
            margin: 0;
            padding-left: 18px;
          }
          .muted {
            color: #6b7280;
          }
          .no-print {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 10px 16px;
            border: none;
            border-radius: 8px;
            background: #111827;
            color: #ffffff;
            cursor: pointer;
            margin-bottom: 20px;
          }
          @media print {
            body {
              background: #ffffff;
              padding: 0;
            }
            .report {
              border: none;
              border-radius: 0;
              max-width: none;
              padding: 0;
            }
            .no-print {
              display: none;
            }
            @page {
              margin: 14mm;
            }
          }
        </style>
      </head>
      <body>
        <button class="no-print" onclick="window.print()">Save as PDF</button>
        <article class="report">
          <h1>${escapeHtml(title)}</h1>
          <div class="meta">Generated on ${escapeHtml(readableDate)}</div>
          <div class="hint">Use the print dialog destination as "Save as PDF" to download this report.</div>
          ${renderValue(data)}
        </article>
        <script>
          window.onload = function () {
            setTimeout(function () {
              window.focus();
              window.print();
            }, 200);
          };
        </script>
      </body>
    </html>
  `);

  reportWindow.document.close();
}