import { useState } from "react";
import { useNavigate } from "react-router";
import { useOrder } from "../contexts/OrderContext";
import { Card, CardHeader, CardTitle, CardContent } from "../components/Card";
import { Button } from "../components/Button";
import { Input } from "../components/Input";
import { Calendar, Clock, MapPin, Phone } from "lucide-react";
import { motion } from "motion/react";

export default function ScheduleAddress() {
  const navigate = useNavigate();
  const { orderData, setOrderData, nextStep, prevStep } = useOrder();

  // Get min date (tomorrow)
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split("T")[0];

  // Get default pickup date (2 days from now)
  const defaultPickupDate = new Date();
  defaultPickupDate.setDate(defaultPickupDate.getDate() + 2);
  const defaultPickupDateStr = defaultPickupDate.toISOString().split("T")[0];

  // Get default delivery date (pickup + 2 days)
  const defaultDeliveryDate = new Date(defaultPickupDate);
  defaultDeliveryDate.setDate(defaultDeliveryDate.getDate() + 2);
  const defaultDeliveryDateStr = defaultDeliveryDate.toISOString().split("T")[0];

  const timeSlots = [
    "08:00 AM - 10:00 AM",
    "10:00 AM - 12:00 PM",
    "02:00 PM - 04:00 PM",
    "04:00 PM - 06:00 PM",
  ];

  const handleNext = () => {
    if (!orderData.address || !orderData.phoneNumber) {
      alert("Please fill in all required fields");
      return;
    }
    nextStep();
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 pt-24 pb-12">
      <div className="max-w-4xl mx-auto px-4 space-y-8">
        {/* Step Header */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-2"
        >
          <div className="flex items-center gap-4">
            <div className="flex items-center justify-center w-10 h-10 rounded-full bg-teal-600 text-white font-bold">
              2
            </div>
            <h1 className="text-3xl font-bold text-slate-900">Schedule & Address</h1>
          </div>
          <p className="text-slate-600 ml-14">Choose pickup and delivery details</p>
        </motion.div>

        {/* Progress Bar */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex gap-2"
        >
          {[1, 2, 3].map((step) => (
            <div key={step} className="flex-1">
              <div
                className={`h-2 rounded-full transition-all ${
                  step <= 2 ? "bg-teal-600" : "bg-slate-300"
                }`}
              />
            </div>
          ))}
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Form */}
          <div className="lg:col-span-2 space-y-6">
            {/* Pickup Schedule */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Calendar className="w-5 h-5 text-blue-600" />
                    Pickup Schedule
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Pickup Date
                      </label>
                      <Input
                        type="date"
                        min={minDate}
                        value={orderData.pickupDate || defaultPickupDateStr}
                        onChange={(e) =>
                          setOrderData({ pickupDate: e.target.value })
                        }
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Preferred Time
                      </label>
                      <select
                        value={orderData.pickupTime || timeSlots[0]}
                        onChange={(e) =>
                          setOrderData({ pickupTime: e.target.value })
                        }
                        className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                      >
                        {timeSlots.map((slot) => (
                          <option key={slot} value={slot}>
                            {slot}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="bg-blue-50 rounded-lg p-3 flex items-start gap-2">
                    <Clock className="w-4 h-4 text-blue-600 mt-0.5 shrink-0" />
                    <p className="text-sm text-blue-800">
                      Our rider will pick up within the selected time window
                    </p>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Delivery Schedule */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.15 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Calendar className="w-5 h-5 text-emerald-600" />
                    Delivery Schedule
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Delivery Date
                      </label>
                      <Input
                        type="date"
                        min={orderData.pickupDate || defaultPickupDateStr}
                        value={orderData.deliveryDate || defaultDeliveryDateStr}
                        onChange={(e) =>
                          setOrderData({ deliveryDate: e.target.value })
                        }
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Preferred Time
                      </label>
                      <select
                        value={orderData.deliveryTime || timeSlots[0]}
                        onChange={(e) =>
                          setOrderData({ deliveryTime: e.target.value })
                        }
                        className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500"
                      >
                        {timeSlots.map((slot) => (
                          <option key={slot} value={slot}>
                            {slot}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="bg-emerald-50 rounded-lg p-3 flex items-start gap-2">
                    <Clock className="w-4 h-4 text-emerald-600 mt-0.5 shrink-0" />
                    <p className="text-sm text-emerald-800">
                      Your clean laundry will be delivered within the selected window
                    </p>
                  </div>
                </CardContent>
              </Card>
            </motion.div>

            {/* Address & Contact */}
            <motion.div
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
            >
              <Card className="border-slate-200 shadow-sm">
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <MapPin className="w-5 h-5 text-teal-600" />
                    Pickup & Delivery Address
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      Full Address *
                    </label>
                    <textarea
                      placeholder="Street address, apartment/unit number, building name"
                      value={orderData.address || ""}
                      onChange={(e) =>
                        setOrderData({ address: e.target.value })
                      }
                      className="w-full p-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none h-24"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 mb-2">
                        Phone Number *
                      </label>
                      <div className="flex gap-2">
                        <select className="w-20 px-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500">
                          <option>+63</option>
                        </select>
                        <Input
                          type="tel"
                          placeholder="9XX XXX XXXX"
                          value={orderData.phoneNumber || ""}
                          onChange={(e) =>
                            setOrderData({
                              phoneNumber: e.target.value,
                            })
                          }
                        />
                      </div>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      Additional Notes (Optional)
                    </label>
                    <textarea
                      placeholder="E.g., Gate code, directions, special access instructions"
                      value={orderData.notes || ""}
                      onChange={(e) =>
                        setOrderData({ notes: e.target.value })
                      }
                      className="w-full p-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-teal-500 resize-none h-16"
                    />
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          </div>

          {/* Summary Sidebar */}
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.25 }}
            className="lg:col-span-1"
          >
            <div className="sticky top-24 space-y-4">
              <Card className="border-slate-200 shadow-sm">
                <CardHeader className="bg-slate-50 pb-4 rounded-t-xl">
                  <CardTitle className="text-lg">Timeline</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4 pt-6">
                  <div className="space-y-4">
                    <div className="flex gap-3">
                      <div className="flex flex-col items-center">
                        <div className="w-3 h-3 rounded-full bg-blue-600" />
                        <div className="w-0.5 h-10 bg-slate-200 my-2" />
                      </div>
                      <div className="pb-4">
                        <p className="font-semibold text-slate-900 text-sm">Pickup</p>
                        <p className="text-xs text-slate-600">
                          {orderData.pickupDate || defaultPickupDateStr}
                        </p>
                        <p className="text-xs text-slate-600">
                          {orderData.pickupTime || timeSlots[0]}
                        </p>
                      </div>
                    </div>

                    <div className="flex gap-3">
                      <div className="flex flex-col items-center">
                        <div className="w-3 h-3 rounded-full bg-emerald-600" />
                      </div>
                      <div>
                        <p className="font-semibold text-slate-900 text-sm">Delivery</p>
                        <p className="text-xs text-slate-600">
                          {orderData.deliveryDate || defaultDeliveryDateStr}
                        </p>
                        <p className="text-xs text-slate-600">
                          {orderData.deliveryTime || timeSlots[0]}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="border-t border-slate-200 pt-4 space-y-2">
                    <p className="text-xs text-slate-600">
                      <strong>Estimated Cost:</strong>
                    </p>
                    <p className="text-2xl font-bold text-teal-600">
                      ₱{(orderData.estimatedPrice || 0).toFixed(2)}
                    </p>
                  </div>
                </CardContent>
              </Card>

              <Button
                onClick={handleNext}
                className="w-full bg-teal-600 hover:bg-teal-700 text-white h-11 rounded-lg font-medium"
              >
                Continue to Payment
              </Button>

              <Button
                onClick={prevStep}
                variant="outline"
                className="w-full border-slate-300"
              >
                Back
              </Button>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
