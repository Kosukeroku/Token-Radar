import { TableHead, TableRow } from "@/components/ui/table"

interface TableHeaderProps {
    showAddedDate?: boolean
    showActions?: boolean
}

export function TableHeader({ showAddedDate, showActions }: TableHeaderProps) {
    return (
        <TableRow>
            <TableHead className="w-12 text-center">#</TableHead>
            <TableHead className="min-w-[200px]">Coin</TableHead>
            <TableHead className="text-right min-w-[120px]">Price</TableHead>
            <TableHead className="text-right min-w-[120px]">24h Change</TableHead>
            <TableHead className="text-right min-w-[140px]">Market Cap</TableHead>
            <TableHead className="text-right min-w-[140px]">Volume (24h)</TableHead>
            {showAddedDate && (
                <TableHead className="text-right min-w-[100px]">Added</TableHead>
            )}
            {showActions && (
                <TableHead className="text-right min-w-[100px]">Actions</TableHead>
            )}
        </TableRow>
    )
}