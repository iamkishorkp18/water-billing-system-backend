ALTER TABLE bills
  ADD COLUMN razorpay_order_id VARCHAR(255),
  ADD COLUMN razorpay_payment_id VARCHAR(255),
  ADD COLUMN paid_at TIMESTAMP;