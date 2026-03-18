const PAYMONGO_BASE = "https://api.paymongo.com/v1";

function getAuth() {
  const key = import.meta.env.VITE_PAYMONGO_SECRET_KEY;
  return btoa(`${key}:`);
}

function headers() {
  return {
    "Content-Type": "application/json",
    Authorization: `Basic ${getAuth()}`,
  };
}

async function handleResponse(res: Response) {
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(
      err.errors?.[0]?.detail ?? `PayMongo error ${res.status}`
    );
  }
  return res.json();
}

// ── Source (GCash / Maya / GrabPay) ──────────────────────────────────────────

export type SourceType = "gcash" | "paymaya" | "grab_pay";

export async function createSource(
  type: SourceType,
  amountPHP: number,
  successUrl: string,
  failedUrl: string
): Promise<string> {
  const res = await fetch(`${PAYMONGO_BASE}/sources`, {
    method: "POST",
    headers: headers(),
    body: JSON.stringify({
      data: {
        attributes: {
          amount: Math.round(amountPHP * 100),
          redirect: { success: successUrl, failed: failedUrl },
          type,
          currency: "PHP",
        },
      },
    }),
  });
  const data = await handleResponse(res);
  return data.data.attributes.redirect.checkout_url as string;
}

// ── Payment Intent + Card Payment Method ────────────────────────────────────

export async function createPaymentIntent(
  amountPHP: number
): Promise<{ id: string; clientKey: string }> {
  const res = await fetch(`${PAYMONGO_BASE}/payment_intents`, {
    method: "POST",
    headers: headers(),
    body: JSON.stringify({
      data: {
        attributes: {
          amount: Math.round(amountPHP * 100),
          payment_method_allowed: ["card"],
          payment_method_options: {
            card: { request_three_d_secure: "any" },
          },
          currency: "PHP",
          capture_type: "automatic",
        },
      },
    }),
  });
  const data = await handleResponse(res);
  return {
    id: data.data.id as string,
    clientKey: data.data.attributes.client_key as string,
  };
}

export async function createCardPaymentMethod(card: {
  number: string;
  expMonth: number;
  expYear: number;
  cvc: string;
  name: string;
  email: string;
}): Promise<string> {
  const res = await fetch(`${PAYMONGO_BASE}/payment_methods`, {
    method: "POST",
    headers: headers(),
    body: JSON.stringify({
      data: {
        attributes: {
          type: "card",
          details: {
            card_number: card.number.replace(/\s/g, ""),
            exp_month: card.expMonth,
            exp_year: card.expYear,
            cvc: card.cvc,
          },
          billing: { name: card.name, email: card.email },
        },
      },
    }),
  });
  const data = await handleResponse(res);
  return data.data.id as string;
}

export async function attachPaymentMethod(
  paymentIntentId: string,
  paymentMethodId: string,
  clientKey: string,
  returnUrl: string
): Promise<{ status: string; redirectUrl?: string }> {
  const res = await fetch(
    `${PAYMONGO_BASE}/payment_intents/${paymentIntentId}/attach`,
    {
      method: "POST",
      headers: headers(),
      body: JSON.stringify({
        data: {
          attributes: {
            payment_method: paymentMethodId,
            client_key: clientKey,
            return_url: returnUrl,
          },
        },
      }),
    }
  );
  const data = await handleResponse(res);
  return {
    status: data.data.attributes.status as string,
    redirectUrl: data.data.attributes.next_action?.redirect?.url as
      | string
      | undefined,
  };
}
