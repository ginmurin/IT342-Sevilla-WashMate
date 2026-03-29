import api from '../utils/api';

export interface ServiceVariantResponse {
  variantId: number;
  variantName: string;
  variantPrice: number;
  displayOrder: number;
  isActive: boolean;
}

export interface ServiceResponse {
  serviceId: number;
  serviceName: string;
  basePricePerUnit: number;
  unitType: string;
  description: string;
  isActive: boolean;
  hasVariants: boolean;
  isAutoSelected: boolean;
  variants?: ServiceVariantResponse[];
  createdAt?: string;
}

export const serviceAPI = {
  getAllServices: async (): Promise<ServiceResponse[]> => {
    const response = await api.get<ServiceResponse[]>('/api/services');
    return response.data;
  }
};

// Direct export for convenience
export const getServices = serviceAPI.getAllServices;